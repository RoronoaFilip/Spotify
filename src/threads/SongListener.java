package threads;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SongListener implements Runnable {
    int port;
    String songName;

    public SongListener(int port, String songName) {
        this.port = port;
        this.songName = songName;
    }

    @Override
    public void run() {
        AudioInputStream inputStream = null;
        try {
            inputStream = AudioSystem.getAudioInputStream(new File(songName + ".wav"));
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
        AudioFormat audioFormat =
            new AudioFormat(inputStream.getFormat().getEncoding(), inputStream.getFormat().getSampleRate(),
                inputStream.getFormat().getSampleSizeInBits(), inputStream.getFormat().getChannels(),
                inputStream.getFormat().getFrameSize(), inputStream.getFormat().getFrameRate(),
                inputStream.getFormat().isBigEndian());
        Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        SourceDataLine dataLine = null;
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        SourceDataLine finalDataLine = dataLine;
        Thread thread = new Thread(() -> {
            try (Socket socket = new Socket("localhost", port);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                 OutputStream stream = socket.getOutputStream()) {
                byte[] toWrite = new byte[audioFormat.getFrameSize()];
                finalDataLine.start();
                try {
                    while (true) {
                        int readBytes = bufferedInputStream.read(toWrite, 0, toWrite.length);
                        finalDataLine.write(toWrite, 0, readBytes);
                        if (!finalDataLine.isRunning()) {
                            break;
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                    //BufferedInputStream has reached the end
                } finally {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException ignored) {
                        throw new RuntimeException("Not yet handled");
                    }
                    System.out.println("Song Stopped");
                }
            } catch (IOException ignored) {
                throw new RuntimeException("Not yet handled");
            }
        });

        thread.start();

        System.out.println("Enter:");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        dataLine.stop();

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Program end");
    }

    public static void main(String[] args) {
        new Thread(new SongListener(7778, "songs/Upsurt-Chekai malko")).start();
    }
}
