package server;

import command.Command;
import command.CommandType;
import command.creator.CommandCreator;
import command.executor.CommandExecutor;
import command.thread.unsafe.LoginCommand;
import database.Database;
import database.InMemoryDatabase;
import server.exceptions.PortCurrentlyStreamingException;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotLoggedInException;
import user.exceptions.UserNotRegisteredException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DefaultSpotifyServer implements StreamingSpotifyServer {
    private static final long STREAMING_PORT = 7000;

    private static final int BUFFER_SIZE = 1024;
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
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); database) {
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

                            System.out.println(clientInput);

                            String output;
                            Command cmd = CommandCreator.create(clientInput, (User) key.attachment(), this);
                            try {
                                checkCommand(cmd, key);
                                output = commandExecutor.execute(cmd);
                            } catch (Exception e) {
                                output = e.getMessage();
                            }

                            verifyLogin(cmd, key);
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

        throw new UserNotRegisteredException("A User with the Name: " + user.username() + " does not exist");
    }

    @Override
    public boolean isLoggedIn(User user) {
        return streamingPortsByUser.containsKey(user);
    }

    @Override
    public long getPort(User user) {
        return streamingPortsByUser.get(user);
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
        new Thread(spotifyServer1).start();
    }
}