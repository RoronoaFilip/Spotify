package database.playlist;

import database.Database;
import database.InMemoryDatabase;
import database.song.Song;
import database.user.User;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Song song = new Song("Recording", "My", "My-Recording.wav", audioFormat);

    static void deleteDirectoryRecursively(Path pathToDelete) throws IOException {
        if (Files.exists(pathToDelete)) {
            File[] allContents = pathToDelete.toFile().listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectoryRecursively(file.toPath());
                }
            }
            Files.delete(pathToDelete);
        }
    }

    @Test
    void testPlaylistOfParsesPlaylistCorrectly() throws IOException {
        Database database = new InMemoryDatabase("songsTestFolder/", "testDatabaseFolder/", "testUsersFile.txt",
            "testPlaylistsFile.txt");
        Playlist actual = PlaylistBase.of(examplePlaylistString, database);

        assertEquals(playlist, actual, "Playlist not parsed correctly");

        Collection<Song> expected = List.of(song);

        assertIterableEquals(expected, actual.getSongs(), "Songs not Parsed or Filtered Correctly");
        database.close();
        deleteDirectoryRecursively(Path.of("testDatabaseFolder/"));
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
