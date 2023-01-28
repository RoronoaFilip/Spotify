package client;

import command.executor.CommandExecutor;
import database.Database;
import database.InMemoryDatabase;
import database.song.Song;
import database.user.User;
import database.user.exceptions.InvalidEmailException;
import database.user.exceptions.UserAlreadyExistsException;
import database.user.exceptions.UserAlreadyLoggedInException;
import database.user.exceptions.UserNotRegisteredException;
import org.junit.jupiter.api.Test;
import server.DefaultSpotifyServer;
import server.SpotifyServerStreamingPermission;
import server.exceptions.PortCurrentlyStreamingException;
import server.streamer.SongStreamer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SongListenerTest {
    private String testSongAudioFormatString = "ok PCM_SIGNED 48000.0 16 1 2 48000.0 false 7000";
    private final AudioFormat audioFormat =
        new AudioFormat(new AudioFormat.Encoding("PCM_SIGNED"), 48000.0f, 16, 1, 2, 48000.0f, false);

    private final Song song = new Song("Recording", "My", "My - Recording.wav", audioFormat);

    private final Database database = new InMemoryDatabase("", "", "", "");
    private final SpotifyServerStreamingPermission spotifyServer =
        new DefaultSpotifyServer(6999, new CommandExecutor(), database);

    private final SpotifyClient client = new SpotifyClient();
    private final User user = new User("filip@", "filip");

    @Test
    void testSongListenerEndsSongCorrectly()
        throws LineUnavailableException, UserAlreadyLoggedInException, UserNotRegisteredException,
        UserAlreadyExistsException, InvalidEmailException {
        database.registerUser("filip@", "filip");
        spotifyServer.logIn(user);
        long port = spotifyServer.getPort(user);

        Thread thread = new Thread(new SongStreamer((int) port, song, spotifyServer));
        thread.setDaemon(true);
        thread.start();

        client.constructSourceDataLine(testSongAudioFormatString);

        assertThrows(PortCurrentlyStreamingException.class, () -> spotifyServer.isPortStreaming(port),
            "PortCurrentlyStreamingException expected");

        assertNotNull(client.getSourceDataLine(), "SourceDataLine must not be null while Song is playing");

        SourceDataLine sourceDataLine = client.getSourceDataLine();
        sourceDataLine.stop();

        assertDoesNotThrow(() -> spotifyServer.isPortStreaming(6999), "No Exception expected");
    }
}
