package song;

import org.junit.jupiter.api.Test;
import song.exceptions.SongNotFoundException;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SongTest {
    private String testSongAudioFormatString = "PCM_SIGNED 44100.0 16 2 4 44100.0 false";

    private AudioFormat audioFormat =
        new AudioFormat(new AudioFormat.Encoding("PCM_SIGNED"), 44100.0f, 16, 2, 4, 44100.0f, false);

    private Song song = new Song("King Of The Fall", "The Weeknd", "The Weeknd-King Of The Fall.wav", audioFormat);

    @Test
    void testSongOfParsesSongFromFileCorrectly() throws SongNotFoundException {
        Song actual = Song.of("songsTestFolder/", "The Weeknd-King Of The Fall.wav");

        assertEquals(song, actual, "Song Name and Singer not Parsed correctly");
        assertEquals(testSongAudioFormatString, actual.getAudioFormatString(),
            "Song Audio Format not extracted correctly");
    }

    @Test
    void testSongOfThrowsSongNotFoundExceptionWhenFileNameDoesNotExist() {
        assertThrows(SongNotFoundException.class,
            () -> Song.of("songsTestFolder/", "a song that - does not exist.wav"), "SongNotFoundException expected");
    }

    @Test
    void testDoFiltersApplyWithAFilterThatApplies() {
        assertTrue(song.doFiltersApply("week", "alabala"), "Expected true when the Filter applies");
        assertFalse(song.doFiltersApply("end", "alabala"), "Expected false when the Filter does not applies");
    }
}
