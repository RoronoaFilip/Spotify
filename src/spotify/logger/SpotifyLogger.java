package spotify.logger;

import spotify.database.user.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpotifyLogger {
    private final String fileName;
    private PrintWriter writer;

    public SpotifyLogger(String fileName) {
        this.fileName = fileName;
        try {
            writer = new PrintWriter(Files.newBufferedWriter(Path.of(fileName)));
        } catch (IOException e) {
            System.out.println("Logs Writer could not be created");
        }
    }

    /**
     * Logs an Error generated from a Command by the current User to the Logs Files
     *
     * @param e           the generated Error
     * @param clientInput the Client Input that triggered the Error
     * @param key         the Selection key to which the current User is attached
     */
    public void log(Exception e, String clientInput, SelectionKey key) {
        User user = (User) key.attachment();

        // Log File
        writer.write("Request <" + clientInput + ">");

        if (user == null) {
            writer.write(" by unknown User");
        } else {
            writer.write(" by User: " + user);
        }
        writer.write(" triggered an Exception:" + System.lineSeparator());
        e.printStackTrace(writer);
        writer.write(System.lineSeparator());

        // Terminal
        System.out.print("Exception " + e.getClass() + " was triggered by ");
        if (user == null) {
            System.out.print("unknown User");
        } else {
            System.out.print("User :" + user);
        }
        System.out.println();
        System.out.println("Exception Message: " + e.getMessage());
        System.out.println("Request that caused it <" + clientInput + ">");
        System.out.println();
    }

    /**
     * Prints the input of the current Socket Channel to the Terminal
     *
     * @param input the input to be printed
     * @param key   the key associated to the Socket Channel
     */
    public void printClientInput(String input, SelectionKey key) {
        User user = (User) key.attachment();

        if (user == null) {
            System.out.println("An unknown User requested <" + input + ">");
        } else {
            System.out.println("User: " + user + " requested <" + input + ">");
        }

        System.out.println();
    }

    /**
     * Prints the out of the current Socket Channel to the Terminal
     *
     * @param output the output to be printed
     * @param key    the key associated to the Socket Channel
     */
    public void printClientOutput(String output, SelectionKey key) {
        User user = (User) key.attachment();

        if (user == null) {
            System.out.println("Sending <" + output + "> to an unknown  User");
        } else {
            System.out.println("Sending <" + output + "> to User: " + user);
        }

        System.out.println();
    }
}
