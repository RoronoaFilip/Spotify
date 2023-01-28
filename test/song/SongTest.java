package song;

import database.song.Song;
import org.junit.jupiter.api.Test;
import database.song.exceptions.SongNotFoundException;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SongTest {
    private String testSongAudioFormatString = "PCM_SIGNED 48000.0 16 1 2 48000.0 false";

    private AudioFormat audioFormat =
        new AudioFormat(new AudioFormat.Encoding("PCM_SIGNED"), 44100.0f, 16, 2, 4, 44100.0f, false);

    private Song song = new Song("Recording", "My", "My-Recording.wav", audioFormat);

    @Test
    void testSongOfParsesSongFromFileCorrectly() throws SongNotFoundException {
        Song actual = Song.of("songsTestFolder/", "My-Recording.wav");

        assertEquals(song, actual, "Song Name and Singer not Parsed correctly");
        assertEquals(testSongAudioFormatString, actual.getAudioFormatString(),
            "Song Audio Format not extracted correctly");
    }

    @Test
    void testSongOfThrowsSongNotFoundExceptionWhenFileNameDoesNotExist() {
        assertThrows(SongNotFoundException.class, () -> Song.of("songsTestFolder/", "a song that - does not exist.wav"),
            "SongNotFoundException expected");
    }

    @Test
    void testDoFiltersApplyWithAFilterThatApplies() {
        assertTrue(song.doFiltersApply("cord", "alabala"), "Expected true when the Filter applies");
        assertFalse(song.doFiltersApply("end", "alabala"), "Expected false when the Filter does not applies");
    }
}
