package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client implements Runnable {
    private static final int SERVER_PORT = 6999;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 512;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private String username;
    private String password;
    private boolean end = false;

    @Override
    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in)) {

            System.out.println("Enter Username:");
            username = scanner.nextLine();

            System.out.println("Enter Password:");
            password = scanner.nextLine();

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");
            while (!end) {
                System.out.print("Enter message: ");
                System.out.println();
                String message = scanner.nextLine(); // read a line from the console

                System.out.println("Sending message <" + message + "> to the server...");

                if ("disconnect".equals(message) || "stop".equals(message)) {
                    end = true;
                }

                writeToServer(message, socketChannel);

                String reply = readServerResponse(socketChannel);

                if (!message.equalsIgnoreCase("play")) {
                    System.out.println("The server replied :" + System.lineSeparator() + reply);
                    System.out.println();
                } else {
                    //TODO: finish
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    private void writeToServer(String message, SocketChannel socketChannel) throws IOException {
        buffer.clear(); // switch to writing mode
        buffer.put(constructMessage(message).getBytes()); // buffer fill
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

    private String constructMessage(String message) {
        return username + "," + password + ":" + message;
    }

    public static void main(String[] args) {
        Client client = new Client();
        new Thread(client).start();
    }
}
