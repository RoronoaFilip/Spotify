package client;

import command.executor.CommandExecutor;
import database.Database;
import database.InMemoryDatabase;
import database.song.Song;
import database.user.User;
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
    private final String testSongAudioFormatString = "PCM_SIGNED 44100.0 16 2 4 44100.0 false 7000";
    private final AudioFormat audioFormat =
        new AudioFormat(new AudioFormat.Encoding("PCM_SIGNED"), 44100.0f, 16, 2, 4, 44100.0f, false);

    private final Song song =
        new Song("King Of The Fall", "The Weeknd", "The Weeknd-King Of The Fall.wav", audioFormat);

    private final Database database = new InMemoryDatabase("songsTestFolder/", "", "", "");
    private final SpotifyServerStreamingPermission spotifyServer =
        new DefaultSpotifyServer(5999, new CommandExecutor(), database);

    private final SpotifyClient client = new SpotifyClient();
    private final User user = new User("filip", "filip");


    @Test
    void testSongListenerEndsSongCorrectly()
        throws LineUnavailableException, UserAlreadyLoggedInException, UserNotRegisteredException,
        UserAlreadyExistsException, InterruptedException {
        database.registerUser("filip", "filip");
        spotifyServer.logIn(user);
        long port = spotifyServer.getPort(user);

        new Thread(new SongStreamer((int) port, song, spotifyServer)).start();

        Thread.sleep(500);
        client.constructSourceDataLine(testSongAudioFormatString);

        assertThrows(PortCurrentlyStreamingException.class, () -> spotifyServer.isPortStreaming(port),
            "PortCurrentlyStreamingException expected");

        assertNotNull(client.getSourceDataLine(), "SourceDataLine must not be null while Song is playing");

        SourceDataLine sourceDataLine = client.getSourceDataLine();
        sourceDataLine.stop();

        assertDoesNotThrow(() -> spotifyServer.isPortStreaming(6999), "No Exception expected");
    }
}
