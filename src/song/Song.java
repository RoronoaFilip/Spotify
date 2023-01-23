package song;

import exceptions.SongFileNotFoundException;
import storage.Storage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Song extends AudioFormat implements Serializable {
    private static final String SINGER_NAME_REGEX = "-";
    private static final int SINGER = 0;
    private static final int NAME = 1;

    public static final String WAV = ".wav";
    private final String name;
    private final String singerName;
    private final String fileName;

    public Song(String name, String singerName, String fileName, AudioFormat format) {
        super(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
            format.getFrameSize(), format.getFrameRate(), format.isBigEndian());

        this.name = name;
        this.fileName = fileName;
        this.singerName = singerName;
    }

    public String getName() {
        return name;
    }

    public String getSingerName() {
        return singerName;
    }

    public String getFileName() {
        return fileName;
    }

    public AudioFormat getAudioFormat() {
        return this;
    }

    public static Song of(String fileName) throws SongFileNotFoundException {
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
            throw new SongFileNotFoundException("A Song with the Name: " + name +
                                                " does not exist");
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

        if (!name.equals(song.name))
            return false;
        return singerName.equals(song.singerName);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + singerName.hashCode();
        return result;
    }

    private static String getNameWithoutExtension(String fileName) {
        int indexOfLastDot = fileName.lastIndexOf('.');

        return fileName.substring(0, indexOfLastDot);
    }
}
