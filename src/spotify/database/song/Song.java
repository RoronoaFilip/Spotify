package spotify.database.song;

import spotify.database.song.exceptions.SongNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a Song in the System
 * A Song can either contain all the Parameters needed to construct an
 * instance of {@code AudioFormat} or not contain them<br>
 * <p>
 * Two Songs are the equal if their Name and the Singer are equal despite Cases
 *
 * <p>Example: "The Weeknd - King Of The Fall" is equal to "the WEEKND    -          KiNg of THE fAll" </p>
 *
 * </p>
 * <p>A Song saves the amount of times it has been streamed by a User</p>
 */
public class Song {
    public static final String SINGER_NAME_REGEX = "(\\s*-\\s*)";
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

    /**
     * Construct a Song with all the Parameters needed to construct an Instance of {@code AudioFormat}<br>
     *
     * @param songName   the Name of the Song
     * @param singerName the Name of the Singer
     * @param fileName   the Name of the File where the Song was read from
     * @param format     - the {@code AudioFormat} of the Song
     */
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

    /**
     * Constructs a Song with only a Song Name and a Singer Name<br>
     * Used to Compare a Song to another since
     * two Songs are the same if their Name and the Singer are the same
     *
     * @param songName   the Name of the Song
     * @param singerName the Name of the Singer
     */
    public Song(String songName, String singerName) {
        this.singerName = singerName;
        this.songName = songName;
        fileName = "";

        streams = new AtomicInteger(0);
    }

    /**
     * Increments the Streams of the Song<br>
     * This is a Thread Safe Operation since multiple Users can listen to the same Song
     */
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

    /**
     * Constructs a String of the Parameters needed to create an Instance of {@code AudioFormat}<br>
     * The Order of the Parameters in the Constructor of {@code AudioFormat} is the same in the constructed String
     *
     * @return a String Concatenation of all Parameters from the Constructor
     * of {@code AudioFormat} separated by 1 Space and their order maintained
     */
    public String getAudioFormatString() {
        return encoding.toString() + " " + sampleRate + " " + sampleSizeInBits + " " + channels + " " + frameSize +
               " " + frameRate + " " + bigEndian;
    }

    /**
     * Checks if any of the {@code filters} applies to this Song<br>
     * A Filter applies to this Song if the Filter is a substring of the Song's Name or Singer name
     *
     * @param filters the Filters to be checked
     * @return true if 1 of the {@code filters} applies to this Song, false if none apply
     */
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

    /**
     * Constructs a Song by reading it from a File in the File System<br>
     *
     * <p>
     * The Song File Name must be Named so:<br>
     * "SingerName"-"SongName".wav
     * </p>
     *
     * <p>
     * Note: a Dash ("-") must be present between the Singer Name and the Song Name<br>
     * The Dash could have any Amount of trailing Whitespaces
     * </p>
     *
     * <p>
     * The Songs File Name however is saved as it is passed as a Parameter.<br>
     * That means that all the trailing Whitespaces in it are not removed
     * </p>
     *
     * @param folderName the Folder where the Song is read from
     * @param fileName   the File Name of the Song
     * @return an Instance of Song with all its Audio Format data included
     * @throws SongNotFoundException if the Song File does not exist or
     *                               an Error occurs while reading the Song from the File
     */
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

    public int getFrameSize() {
        return frameSize;
    }
}
