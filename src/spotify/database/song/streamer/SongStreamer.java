package spotify.database.song.streamer;

import spotify.database.song.Song;
import spotify.server.SpotifyServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A Thread that reads a Song from the File System and sends the read Info through
 * a blocking java.net Communication.<br>
 * The Thread has SpotifyServerStreaming Permission so that it can mark a specific Port as
 * "Currently Streaming" in the Server so that another SongStreamer is not started for the same Port.
 *
 * <p>
 * If the Song is stopped from the other side of the java.net Communication
 * (the Thread on the other Side stops, which would trigger a {@code SocketException} in this Thread)
 * or when the Song Reading Stream on this side ends,
 * the Port is marked as free a SongStreamer can be started again for that Port
 * </p>
 *
 * <p>
 * When the Song ends/is stopped its Streams Counter is incremented
 * </p>
 *
 * <p>
 * This Thread should be marked as a Daemon Thread before it is started so that it doesn't
 * stall the Program if it ends while this Thread is Running
 * </p>
 */
public class SongStreamer implements Runnable {
    private final int port;
    private final Song song;
    private final SpotifyServer spotifyServer;

    public SongStreamer(int port, Song song, SpotifyServer spotifyServer) {
        this.port = port;
        this.song = song;
        this.spotifyServer = spotifyServer;
    }

    @Override
    public void run() {
        spotifyServer.getUserService().lockPort(port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (Socket socket = serverSocket.accept();
                 BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(
                     Path.of(spotifyServer.getDatabase().getSongsFolder() + song.getFileName())))) {

                byte[] toWrite = new byte[song.getFrameSize()];
                while (bufferedInputStream.available() > 0) {
                    int readBytes = bufferedInputStream.read(toWrite, 0, toWrite.length);

                    outputStream.write(toWrite, 0, readBytes);
                }

                outputStream.flush();
            } catch (SocketException ignored) {
                //The User has Stopped The Song
            }
        } catch (IOException e) {
            System.out.println("A Problem occurred while streaming Song");
        }

        song.stream();
        spotifyServer.getUserService().freePort(port);

        System.out.println("Song has ended");
    }
}
