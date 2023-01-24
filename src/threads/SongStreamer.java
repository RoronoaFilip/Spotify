package threads;

import song.Song;
import song.exceptions.SongNotFoundException;
import storage.Storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongStreamer implements Runnable {
    int port;
    Song song;

    public SongStreamer(int port, Song song) {
        this.port = port;
        this.song = song;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (Socket socket = serverSocket.accept();
                 BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(
                     Files.newInputStream(Path.of(Storage.SONGS_FOLDER_NAME + song.getFileName())))) {

                byte[] toWrite = new byte[song.getFrameSize()];
                while (bufferedInputStream.available() > 0) {
                    int readBytes = bufferedInputStream.read(toWrite, 0, toWrite.length);

                    outputStream.write(toWrite, 0, readBytes);
                }
                System.out.println("Song Stopped");
            } catch (SocketException ignored) {
                //The User has Stopped The Song
                System.out.println("The User has Stopped The Song");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Song has ended");
    }

    public static void main(String[] args) throws SongNotFoundException {
        Song song = Song.of("Upsurt-Chekai malko.wav");

        Thread thread = new Thread(new SongStreamer(7778, song));
        thread.start();
    }
}
