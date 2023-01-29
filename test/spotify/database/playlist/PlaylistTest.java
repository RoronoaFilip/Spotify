package spotify.database.playlist;

import org.junit.jupiter.api.Test;
import spotify.database.Database;
import spotify.database.InMemoryDatabase;
import spotify.database.song.Song;
import spotify.database.user.User;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaylistTest {
    private String examplePlaylistString =
        "filip,123:myPlaylist:My-Recording, A Song That Does Not Exist, Not Even a Proper Song Name  Format";
    private User user = new User("filip", "123");
    private Playlist playlist = new PlaylistBase("myPlaylist", user);

    private String testSongAudioFormatString = "PCM_SIGNED 44100.0 16 2 4 44100.0 false";

    private AudioFormat audioFormat =
        new AudioFormat(new AudioFormat.Encoding("PCM_SIGNED"), 48000.0f, 16, 1, 2, 48000.0f, false);

    private Song song = new Song("Recording", "My", "My - Recording.wav", audioFormat);

    @Test
    void testPlaylistOfParsesPlaylistCorrectly() {
        Database database =
            new InMemoryDatabase("", "testDatabaseFolder/", "testUsersFile.txt", "testPlaylistsFile.txt");
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
