package spotify.client;

import spotify.database.song.listener.SongListener;

import javax.naming.OperationNotSupportedException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class SpotifyClient implements Runnable {
    private static final String CLIENT_LOG_FILE_NAME = "clientLogs.txt";
    private static final int RESPONSE_STATUS_INDEX = 0;
    private static final int ENCODING_INDEX = 1;
    private static final int SAMPLE_RATE_INDEX = 2;
    private static final int SAMPLE_SIZE_IN_BITS_INDEX = 3;
    private static final int CHANNELS_INDEX = 4;
    private static final int FRAME_SIZE_INDEX = 5;
    private static final int FRAME_RATE_INDEX = 6;
    private static final int BIG_ENDIAN_INDEX = 7;
    private static final int PORT_INDEX = 8;

    private static final int SERVER_PORT = 6999;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private boolean end = false;
    private SourceDataLine sourceDataLine = null;

    @Override
    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in);
             PrintWriter logsWriter = new PrintWriter(Files.newBufferedWriter(Path.of(CLIENT_LOG_FILE_NAME)))) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");
            while (!end) {
                System.out.println("Enter message: ");
                String message = scanner.nextLine();

                if ("disconnect".equals(message) || "terminate".equals(message)) {
                    end = true;
                }

                if ("stop".equalsIgnoreCase(message)) {
                    try {
                        stopSong();
                        logMessage("Song stopped", logsWriter);
                    } catch (OperationNotSupportedException e) {
                        logException(new Exception("Nothing to stop. No Song is playing", e), logsWriter);
                    }
                    continue;
                }

                logMessage("Sending message <" + message + "> to the server", logsWriter);
                writeToServer(message, socketChannel);

                String reply = readServerResponse(socketChannel);
                logMessage("The server replied :" + System.lineSeparator() + reply, logsWriter);

                if (message.startsWith("play")) {
                    try {
                        constructSourceDataLine(reply);
                    } catch (LineUnavailableException e) {
                        logException(new Exception("Streaming failed", e), logsWriter);
                    }
                }
            }

            try {
                stopSong();
                logMessage("Song stopped", logsWriter);
            } catch (OperationNotSupportedException ignore) {
                // No Song is Playing
            } finally {
                logMessage("Ending Program", logsWriter);
            }

        } catch (IOException e) {
            System.out.println("The Server is offline");
        }
    }

    private void logMessage(String message, PrintWriter logger) {
        System.out.println(message);
        System.out.println();

        logger.println(message);
        logger.println();
    }

    private void logException(Exception e, PrintWriter logger) {
        e.printStackTrace(logger);
        logger.println();
    }

    public void constructSourceDataLine(String reply) throws LineUnavailableException {
        String[] splitReply = reply.split("\\s+");

        if (!splitReply[RESPONSE_STATUS_INDEX].equalsIgnoreCase("ok")) {
            return;
        }

        AudioFormat audioFormat = new AudioFormat(new AudioFormat.Encoding(splitReply[ENCODING_INDEX]),
            Float.parseFloat(splitReply[SAMPLE_RATE_INDEX]), Integer.parseInt(splitReply[SAMPLE_SIZE_IN_BITS_INDEX]),
            Integer.parseInt(splitReply[CHANNELS_INDEX]), Integer.parseInt(splitReply[FRAME_SIZE_INDEX]),
            Float.parseFloat(splitReply[FRAME_RATE_INDEX]), Boolean.parseBoolean(splitReply[BIG_ENDIAN_INDEX]));

        int streamingPort = Integer.parseInt(splitReply[PORT_INDEX]);

        Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open();

        System.out.println("Playing Song:");
        new Thread(new SongListener(streamingPort, sourceDataLine, this), "Song Listener").start();
    }

    private void stopSong() throws OperationNotSupportedException {
        if (sourceDataLine == null) {
            throw new OperationNotSupportedException("Nothing to stop");
        }

        sourceDataLine.stop();
    }

    public void resetSourceDataLine() {
        sourceDataLine = null;
    }

    private void writeToServer(String message, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes());

        buffer.flip();
        socketChannel.write(buffer);
    }

    private String readServerResponse(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);

        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);

        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public SourceDataLine getSourceDataLine() {
        return sourceDataLine;
    }

    public static void main(String[] args) {
        SpotifyClient spotifyClient = new SpotifyClient();
        new Thread(spotifyClient, "Spotify Client Thread").start();
    }
}