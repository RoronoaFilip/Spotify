package client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class SpotifyClient implements Runnable {
    private static final int AUDIO_FORMAT_RESPONSE_SIZE = 8;
    private static final int ENCODING_INDEX = 0;
    private static final int SAMPLE_RATE_INDEX = 1;
    private static final int SAMPLE_SIZE_IN_BITS_INDEX = 2;
    private static final int CHANNELS_INDEX = 3;
    private static final int FRAME_SIZE_INDEX = 4;
    private static final int FRAME_RATE_INDEX = 5;
    private static final int BIG_ENDIAN_INDEX = 6;
    private static final int PORT_INDEX = 7;

    private static final int SERVER_PORT = 6999;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private boolean end = false;
    private SourceDataLine sourceDataLine = null;

    @Override
    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");
            while (!end) {
                System.out.print("Enter message: ");
                System.out.println();
                String message = scanner.nextLine(); // read a line from the console

                if ("disconnect".equals(message) || "terminate".equals(message)) {
                    end = true;
                }

                if ("stop".equalsIgnoreCase(message)) {
                    stopSong();
                    continue;
                }

                System.out.println("Sending message <" + message + "> to the server...");
                writeToServer(message, socketChannel);

                String reply = readServerResponse(socketChannel);

                if (!message.startsWith("play ")) {
                    System.out.println("The server replied :" + System.lineSeparator() + reply);
                } else {
                    try {
                        System.out.println("Playing Song:");
                        constructSourceDataLine(reply);
                    } catch (LineUnavailableException e) {
                        System.out.println("Streaming failed");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("The Server is offline");
        }

        stopSong();
    }

    public void constructSourceDataLine(String reply) throws LineUnavailableException {
        String[] splitReply = reply.split("\\s+");

        if (splitReply.length != AUDIO_FORMAT_RESPONSE_SIZE) {
            System.out.println(reply);
            return;
        }

        AudioFormat audioFormat =
            new AudioFormat(new AudioFormat.Encoding(splitReply[ENCODING_INDEX]),
                Float.parseFloat(splitReply[SAMPLE_RATE_INDEX]),
                Integer.parseInt(splitReply[SAMPLE_SIZE_IN_BITS_INDEX]),
                Integer.parseInt(splitReply[CHANNELS_INDEX]),
                Integer.parseInt(splitReply[FRAME_SIZE_INDEX]),
                Float.parseFloat(splitReply[FRAME_RATE_INDEX]),
                Boolean.parseBoolean(splitReply[BIG_ENDIAN_INDEX]));

        int streamingPort = Integer.parseInt(splitReply[PORT_INDEX]);

        Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open();
        new Thread(new SongListener(streamingPort, sourceDataLine, this)).start();
    }

    private void stopSong() {
        if (sourceDataLine == null) {
            System.out.println("Nothing to Stop");
            return;
        }

        sourceDataLine.stop();
        System.out.println("Song stopped");
    }

    public void resetSourceDataLine() {
        sourceDataLine = null;
    }

    private void writeToServer(String message, SocketChannel socketChannel) throws IOException {
        buffer.clear(); // switch to writing mode
        buffer.put(message.getBytes()); // buffer fill
        buffer.flip(); // switch to reading mode
        socketChannel.write(buffer); // buffer drain
    }

    private String readServerResponse(SocketChannel socketChannel) throws IOException {
        buffer.clear(); // switch to writing mode
        socketChannel.read(buffer); // buffer fill
        buffer.flip(); // switch to reading mode

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, "UTF-8"); // buffer drain
    }

    public SourceDataLine getSourceDataLine() {
        return sourceDataLine;
    }

    public static void main(String[] args) {
        SpotifyClient spotifyClient = new SpotifyClient();
        new Thread(spotifyClient).start();
    }
}
