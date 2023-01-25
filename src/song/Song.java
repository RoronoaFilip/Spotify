package song;

import song.exceptions.SongNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class Song {
    public static final String SINGER_NAME_REGEX = "-|\\s+-\\s+";
    public static final String SINGER_NAME_CONCATENATION = " - ";
    private static final int SINGER = 0;
    private static final int NAME = 1;

    public static final String WAV = ".wav";
    private final String singerName;
    private final String songName;
    private final String fileName;
    private final AtomicInteger streams;

    private AudioFormat.Encoding encoding;
    private float sampleRate;
    private int sampleSizeInBits;
    private int channels;
    private int frameSize;
    private float frameRate;
    private boolean bigEndian;

    public Song(String songName, String singerName, String fileName, AudioFormat format) {
        encoding = format.getEncoding();
        sampleRate = format.getSampleRate();
        sampleSizeInBits = format.getSampleSizeInBits();
        channels = format.getChannels();
        frameSize = format.getFrameSize();
        frameRate = format.getFrameRate();
        bigEndian = format.isBigEndian();

        this.singerName = singerName;
        this.songName = songName;
        this.fileName = fileName;

        streams = new AtomicInteger(0);
    }

    public Song(String songName, String singerName) {
        this.singerName = singerName;
        this.songName = songName;
        fileName = "";

        streams = new AtomicInteger(0);
    }

    public void stream() {
        streams.incrementAndGet();
    }

    public int getStreams() {
        return streams.get();
    }

    public String getSongName() {
        return songName;
    }

    public String getSingerName() {
        return singerName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAudioFormatString() {
        return encoding.toString() + " " + sampleRate + " " + sampleSizeInBits + " " + channels + " " + frameSize +
               " " + frameRate + " " + bigEndian;
    }

    public boolean doFiltersApply(String... filters) {
        String nameLowerCase = songName.toLowerCase(Locale.ROOT);
        String singerLowerCase = singerName.toLowerCase(Locale.ROOT);

        for (String filter : filters) {
            String filterLowerCase = filter.toLowerCase(Locale.ROOT);

            if (nameLowerCase.contains(filterLowerCase) || singerLowerCase.contains(filterLowerCase)) {
                return true;
            }
        }

        return false;
    }

    public static Song of(String folderName, String fileName) throws SongNotFoundException {
        String wholeName = getNameWithoutExtension(fileName);

        String[] splitWholeName = wholeName.split(SINGER_NAME_REGEX);

        if (splitWholeName.length != 2) {
            throw new SongNotFoundException("A Song File with the Name: " + fileName + " does not exist");
        }

        String name = splitWholeName[NAME];
        String singerName = splitWholeName[SINGER];

        Song toReturn;
        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(folderName + fileName))) {

            AudioFormat audioFormat = inputStream.getFormat();
            toReturn = new Song(name, singerName, fileName, audioFormat);

        } catch (UnsupportedAudioFileException | IOException e) {
            throw new SongNotFoundException("A Song with the Name: " + name + " does not exist");
        }

        return toReturn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Song song = (Song) o;

        if (!songName.equalsIgnoreCase(song.songName))
            return false;
        return singerName.equalsIgnoreCase(song.singerName);
    }

    @Override
    public int hashCode() {
        String songNameLowerCase = songName.toLowerCase();
        String singerNameLowerCase = songName.toLowerCase();

        int result = songNameLowerCase.hashCode();
        result = 31 * result + singerNameLowerCase.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return singerName + SINGER_NAME_CONCATENATION + songName;
    }

    private static String getNameWithoutExtension(String fileName) {
        int indexOfLastDot = fileName.lastIndexOf('.');

        if (indexOfLastDot == -1) { // Not a File Name
            return fileName;
        }

        return fileName.substring(0, indexOfLastDot);
    }
}
