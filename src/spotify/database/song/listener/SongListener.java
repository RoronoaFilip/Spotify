package spotify.database.song.listener;

import spotify.client.SpotifyClient;

import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class SongListener implements Runnable {
    int port;
    SourceDataLine dataLine;
    SpotifyClient spotifyClient;

    public SongListener(int port, SourceDataLine dataLine, SpotifyClient spotifyClient) {
        this.port = port;
        this.dataLine = dataLine;
        this.spotifyClient = spotifyClient;
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

                    if (readBytes == -1) {
                        break;
                    }

                    dataLine.write(toWrite, 0, readBytes);
                } while (dataLine.isRunning());

            } catch (IllegalArgumentException ignored) {
                //BufferedInputStream has reached the end
            }
        } catch (IOException ignored) {
            System.out.println("No Song Streamer detected");
        }

        dataLine.drain();
        dataLine.close();

        spotifyClient.resetSourceDataLine();
    }
}
