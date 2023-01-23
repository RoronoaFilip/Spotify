package song;

import song.exceptions.SongNotFoundException;
import storage.Storage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Song {
    private static final String SINGER_NAME_REGEX = "-";
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

    Song(String songName, String singerName, String fileName, AudioFormat format) {
        encoding = format.getEncoding();
        sampleRate = format.getSampleRate();
        sampleSizeInBits = format.getSampleSizeInBits();
        channels = format.getChannels();
        frameSize = format.getFrameSize();
        frameRate = format.getFrameRate();
        bigEndian = format.isBigEndian();

        this.songName = songName;
        this.fileName = fileName;
        this.singerName = singerName;
        streams = new AtomicInteger(0);
    }

    public Song(String songName, String singerName) {
        this.singerName = singerName;
        this.songName = songName;
        fileName = "";
        streams = new AtomicInteger(0);
    }

    public void stream() {
        streams.getAndIncrement();
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
        return encoding.toString() + " " + sampleRate + " " + sampleSizeInBits + " " + channels +
               " " + frameSize + " " + frameRate + " " + bigEndian;
    }

    public static Song of(String fileName) throws SongNotFoundException {
        String wholeName = getNameWithoutExtension(fileName);

        String[] splitWholeName = wholeName.split(SINGER_NAME_REGEX);

        String name = splitWholeName[NAME];
        String singerName = splitWholeName[SINGER];

        Song toReturn = null;
        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(
            new File(Storage.getFolderName() + fileName))) {

            AudioFormat audioFormat = inputStream.getFormat();
            toReturn = new Song(name, singerName, fileName, audioFormat);

        } catch (UnsupportedAudioFileException e) {
            throw new SongNotFoundException("A Song with the Name: " + name + " does not exist");
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        if (!songName.equals(song.songName))
            return false;
        return singerName.equals(song.singerName);
    }

    @Override
    public int hashCode() {
        int result = songName.hashCode();
        result = 31 * result + singerName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return singerName + SINGER_NAME_REGEX + songName;
    }

    private static String getNameWithoutExtension(String fileName) {
        int indexOfLastDot = fileName.lastIndexOf('.');

        return fileName.substring(0, indexOfLastDot);
    }
}
