package threads;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongStreamer implements Runnable {
    int port;
    String songName;

    public SongStreamer(int port, String songName) {
        this.port = port;
        this.songName = songName;
    }

    @Override
    public void run() {
        AudioInputStream inputStream = null;
        try {
            inputStream = AudioSystem.getAudioInputStream(new File(songName + ".wav"));
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AudioFormat audioFormat =
            new AudioFormat(inputStream.getFormat().getEncoding(), inputStream.getFormat().getSampleRate(),
                inputStream.getFormat().getSampleSizeInBits(), inputStream.getFormat().getChannels(),
                inputStream.getFormat().getFrameSize(), inputStream.getFormat().getFrameRate(),
                inputStream.getFormat().isBigEndian());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (Socket socket = serverSocket.accept();
                 BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                 InputStream stream = socket.getInputStream();
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(
                     Files.newInputStream(Path.of(songName + ".wav")))) {

                byte[] toWrite = new byte[audioFormat.getFrameSize()];
                while (bufferedInputStream.available() > 0) {
                    int readBytes = bufferedInputStream.read(toWrite, 0, toWrite.length);

                    outputStream.write(toWrite, 0, readBytes);
                }
                System.out.println("Song Stopped");
            }
        } catch (SocketException ignored) {
            //The User has Stopped The Song
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Thread(new SongStreamer(7778, "songs/Upsurt-Chekai malko")).start();
    }
}
