package threads;

import song.Song;
import song.exceptions.SongNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class SongListener implements Runnable {
    int port;
    SourceDataLine dataLine;

    public SongListener(int port, SourceDataLine dataLine) {
        this.port = port;
        this.dataLine = dataLine;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket("localhost", port);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream())) {

            byte[] toWrite = new byte[dataLine.getFormat().getFrameSize()];
            dataLine.start();
            try {
                do {
                    int readBytes = bufferedInputStream.read(toWrite, 0, toWrite.length);
                    dataLine.write(toWrite, 0, readBytes);
                } while (dataLine.isRunning());
            } catch (IllegalArgumentException ignored) {
                //BufferedInputStream has reached the end
            }
        } catch (IOException ignored) {
            System.out.println("No Song Streamer detected");
            return;
        }

        System.out.println("Song end");
    }

    public static void main(String[] args) throws SongNotFoundException, LineUnavailableException {
        Song song = Song.of("Upsurt-Chekai malko.wav");
        AudioFormat audioFormat =
            new AudioFormat(song.getEncoding(), song.getSampleRate(), song.getSampleSizeInBits(), song.getChannels(),
                song.getFrameSize(), song.getFrameRate(), song.isBigEndian());
        Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        SourceDataLine dataLine = null;

        dataLine = (SourceDataLine) AudioSystem.getLine(info);
        dataLine.open();

        new Thread(new SongStreamer(7778, song)).start();
        new Thread(new SongListener(7778, dataLine)).start();

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        dataLine.stop();
    }
}
