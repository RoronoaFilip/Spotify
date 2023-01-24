package client;

import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class SongListener implements Runnable {
    int port;
    SourceDataLine dataLine;
    Client client;

    public SongListener(int port, SourceDataLine dataLine, Client client) {
        this.port = port;
        this.dataLine = dataLine;
        this.client = client;
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
            client.resetSourceDataLine();
        }

        client.resetSourceDataLine();
    }
}
