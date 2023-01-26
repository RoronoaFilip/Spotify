package server;

import command.Command;
import command.CommandType;
import command.creator.CommandCreator;
import command.executor.CommandExecutor;
import command.thread.unsafe.LoginCommand;
import database.Database;
import database.InMemoryDatabase;
import database.user.User;
import database.user.exceptions.UserAlreadyLoggedInException;
import database.user.exceptions.UserNotLoggedInException;
import database.user.exceptions.UserNotRegisteredException;
import server.exceptions.PortCurrentlyStreamingException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DefaultSpotifyServer implements SpotifyServerTerminatePermission {
    private static final String LOG_FILE_NAME = "logs.txt";

    private static final long STREAMING_PORT = 7000;

    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;
    private final Database database;

    private final int port;
    private boolean isServerWorking;

    private final ByteBuffer buffer;
    private Selector selector;

    private final Map<User, Long> streamingPortsByUser;
    private final Map<User, SelectionKey> selectionKeysByUser;
    private final Set<Long> currentlyStreamingPorts;
    private final TreeSet<Long> ports;
    private final Object currentSessionLock = new Object();

    public DefaultSpotifyServer(int port, CommandExecutor commandExecutor, Database database) {
        this.port = port;
        this.commandExecutor = commandExecutor;

        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

        streamingPortsByUser = new HashMap<>();
        selectionKeysByUser = new HashMap<>();
        currentlyStreamingPorts = new HashSet<>();
        ports = new TreeSet<>();

        this.database = database;
        isServerWorking = true;
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); database;
             PrintWriter logsWriter = new PrintWriter(Files.newBufferedWriter(Path.of(LOG_FILE_NAME)))) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();

                            String clientInput = getClientInput(clientChannel);
                            if (clientInput == null) {
                                continue;
                            }

                            printClientInput(clientInput, key);

                            String output;
                            Command cmd = CommandCreator.create(clientInput, (User) key.attachment(), this);
                            try {
                                checkCommand(cmd, key);
                                output = commandExecutor.execute(cmd);
                                verifyLogin(cmd, key);
                            } catch (Exception e) {
                                log(e, clientInput, key, logsWriter);
                                output = e.getMessage();
                            }

                            printClientOutput(output, key);
                            writeClientOutput(clientChannel, output);
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("failed to start server");
        }
    }

    /**
     * Logs an Error generated from a Command by the current User to the Logs Files
     *
     * @param e           the generated Error
     * @param clientInput the Client Input that triggered the Error
     * @param key         the Selection key to which the current User is attached
     * @param writer      a PrintWriter to the Logs File
     */
    private void log(Exception e, String clientInput, SelectionKey key, PrintWriter writer) {
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
    private void printClientInput(String input, SelectionKey key) {
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
    private void printClientOutput(String output, SelectionKey key) {
        User user = (User) key.attachment();

        if (user == null) {
            System.out.println("Sending <" + output + "> to an unknown  User");
        } else {
            System.out.println("Sending <" + output + "> to User: " + user);
        }

        System.out.println();
    }

    /**
     * Verifies if the Login Command has been executed successfully
     * <p>
     * If the Login Command has been executed successfully the User that the Command
     * is tied to gets attached to the current Selection Key
     * </p>
     * <p>
     * If the Login Command has not been executed successfully nothing happens
     * and the User receives the Message generated by the failed Login Command
     * </p>
     *
     * @param cmd the Command to be checked
     * @param key the User's Selection key
     */
    private void verifyLogin(Command cmd, SelectionKey key) {
        if (cmd == null || cmd.getType() != CommandType.LOGIN_COMMAND) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) cmd;

        if (!loginCommand.isSuccessful()) {
            return;
        }

        User user = loginCommand.getUser();
        attach(user, key);
    }

    /**
     * Attaches User to his Selection Key
     *
     * @param user the User to be attached
     * @param key  the User's Selection Key
     */
    private void attach(User user, SelectionKey key) {
        key.attach(user);
        selectionKeysByUser.put(user, key);
    }

    @Override
    public void terminate() {
        this.isServerWorking = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Checks if the {@code command} is valid for the User attached to the {@code key}
     *
     * <p>
     * A User that has not logged in can only log in and register in to the System
     * </p>
     * <p>
     * A User that has logged in can do everything except log in or register in to the System
     * </p>
     *
     * @param command the Command to be checked
     * @param key     the Selection Key to which a User is attached
     * @return null if the User does not have Permission to execute said Command, the Command itself otherwise
     * @throws UserNotLoggedInException     if the User has not logged in to the System
     * @throws UserAlreadyLoggedInException if the User has already been logged in to the System
     */
    private Command checkCommand(Command command, SelectionKey key)
        throws UserNotLoggedInException, UserAlreadyLoggedInException {
        if (command == null) {
            return null;
        }
        boolean isLoggedIn = key.attachment() != null;

        if (!isLoggedIn) {
            if (command.getType() == CommandType.REGISTER_COMMAND || command.getType() == CommandType.LOGIN_COMMAND) {
                return command;
            }

            throw new UserNotLoggedInException("You have not logged in");
        }

        if (command.getType() == CommandType.LOGIN_COMMAND || command.getType() == CommandType.REGISTER_COMMAND) {
            throw new UserAlreadyLoggedInException("You have already logged in");
        }

        return command;
    }

    @Override
    public void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        attachStreamingPort(user);
    }

    @Override
    public void logOut(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        detachStreamingPort(user);
        selectionKeysByUser.remove(user);
    }

    private void attachStreamingPort(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkRegistered(user);

            if (isLoggedIn(user)) {
                throw new UserAlreadyLoggedInException("User already logged in");
            }

            if (streamingPortsByUser.isEmpty()) {
                streamingPortsByUser.put(user, STREAMING_PORT);
                ports.add(STREAMING_PORT);
                return;
            }

            long nextKey = ports.last() + 1;
            streamingPortsByUser.put(user, nextKey);
            ports.add(nextKey);
        }
    }

    private void detachStreamingPort(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkRegistered(user);

            if (!isLoggedIn(user)) {
                throw new UserNotLoggedInException("User not logged in");
            }

            ports.remove(streamingPortsByUser.get(user));
            streamingPortsByUser.remove(user);
        }
    }

    private void checkRegistered(User user) throws UserNotRegisteredException {
        if (database.doesUserExist(user)) {
            return;
        }

        throw new UserNotRegisteredException(
            "A User with Email: " + user.email() + " and Password: " + user.password() + " does not exist");
    }

    @Override
    public boolean isLoggedIn(User user) {
        return streamingPortsByUser.containsKey(user);
    }

    @Override
    public long getPort(User user) {
        if (streamingPortsByUser.containsKey(user)) {
            return streamingPortsByUser.get(user);

        }

        return -1;
    }

    @Override
    public void addPortStreaming(long port) {
        if (!ports.contains(port)) {
            return;
        }

        currentlyStreamingPorts.add(port);
    }

    @Override
    public void isPortStreaming(long port) throws PortCurrentlyStreamingException {
        if (currentlyStreamingPorts.contains(port)) {
            throw new PortCurrentlyStreamingException(
                "You are currently listening to a Song. Stop the current one and than try again");
        }
    }

    @Override
    public void removePortStreaming(long port) {
        try {
            isPortStreaming(port);
        } catch (PortCurrentlyStreamingException ignore) {
            // Ignore - this is what we want
        }

        currentlyStreamingPorts.remove(port);
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    public Map<User, Long> getStreamingPortsByUser() {
        return streamingPortsByUser;
    }

    public Map<User, SelectionKey> getSelectionKeysByUser() {
        return selectionKeysByUser;
    }

    public Set<Long> getCurrentlyStreamingPorts() {
        return currentlyStreamingPorts;
    }

    public TreeSet<Long> getPorts() {
        return ports;
    }

    public static void main(String[] args) {
        final int port = 6999;
        DefaultSpotifyServer spotifyServer1 = new DefaultSpotifyServer(port, new CommandExecutor(),
            new InMemoryDatabase(Database.SONGS_FOLDER_DEFAULT, Database.DATABASE_FOLDER_DEFAULT,
                Database.USERS_FILE_NAME_DEFAULT, Database.PLAYLISTS_FILE_NAME_DEFAULT));
        new Thread(spotifyServer1, "Spotify Server Thread").start();
    }
}