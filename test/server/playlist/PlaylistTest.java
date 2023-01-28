package server.playlist;

import database.Database;
import database.InMemoryDatabase;
import database.playlist.Playlist;
import database.playlist.PlaylistBase;
import database.song.Song;
import database.user.User;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaylistTest {
    private String examplePlaylistString =
        "filip,123:myPlaylist:My-Recording,A Song That - Does Not Exist, Not Even a Proper Song Name  Format";
    private User user = new User("filip", "123");
    private Playlist playlist = new PlaylistBase("myPlaylist", user);

    private String testSongAudioFormatString = "PCM_SIGNED 44100.0 16 2 4 44100.0 false";

    private Database database = new InMemoryDatabase("songsTestFolder/", "", "", "");

    private AudioFormat audioFormat =
        new AudioFormat(new AudioFormat.Encoding("PCM_SIGNED"), 44100.0f, 16, 2, 4, 44100.0f, false);

    private Song song = new Song("Recording", "My", "My-Recording.wav", audioFormat);

    @Test
    void testPlaylistOfParsesPlaylistCorrectly() {
        Playlist actual = PlaylistBase.of(examplePlaylistString, database);

        assertEquals(playlist, actual, "Playlist not parsed correctly");

        Collection<Song> expected = List.of(song);

        assertIterableEquals(expected, actual.getSongs(), "Songs not Parsed or Filtered Correctly");
    }

    @Test
    void testContainsSongWorkCorrectly() {
        Song newSong = new Song("filip", "filip");
        Song notANewSong = new Song("notFillip", "notFilip");

        playlist.addSong(newSong);

        assertTrue(playlist.containsSong(newSong), "True expected when a Song is in the playlist");
        assertFalse(playlist.containsSong(notANewSong), "False expected when a Song is not in the playlist");
    }
}
