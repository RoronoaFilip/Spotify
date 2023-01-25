package server.streamer;

import server.SpotifyServerStreamingPermission;
import song.Song;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongStreamer implements Runnable {
    private final int port;
    private final Song song;
    private final SpotifyServerStreamingPermission spotifyServer;

    public SongStreamer(int port, Song song, SpotifyServerStreamingPermission spotifyServer) {
        this.port = port;
        this.song = song;
        this.spotifyServer = spotifyServer;
    }

    @Override
    public void run() {
        spotifyServer.addPortStreaming(port);

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
            } catch (SocketException ignored) {
                //The User has Stopped The Song
            }
        } catch (IOException e) {
            System.out.println("A Problem occurred while streaming Song");
        }

        song.stream();
        spotifyServer.removePortStreaming(port);

        System.out.println("Song has ended");
    }
}
