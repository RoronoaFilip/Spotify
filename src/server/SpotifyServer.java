package server;

import command.Command;
import command.CommandType;
import command.creator.CommandCreator;
import command.executor.CommandExecutor;
import database.InMemoryDatabase;
import database.Database;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotLoggedInException;
import user.exceptions.UserNotRegisteredException;

import java.io.IOException;
import java.io.UncheckedIOException;
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

public class SpotifyServer implements Runnable {
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

    public SpotifyServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;

        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

        streamingPortsByUser = new HashMap<>();
        selectionKeysByUser = new HashMap<>();
        currentlyStreamingPorts = new HashSet<>();
        ports = new TreeSet<>();

        database = new InMemoryDatabase();
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
                            System.out.println(clientInput);
                            if (clientInput == null) {
                                continue;
                            }

                            String output;
                            try {
                                Command cmd = CommandCreator.create(clientInput, key, this);
                                checkCommand(cmd, key);
                                output = commandExecutor.execute(cmd);
                            } catch (UserNotLoggedInException | UserAlreadyLoggedInException e) {
                                output = e.getMessage();
                            }

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
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    private void attach(User user, SelectionKey key) {
        key.attach(user);
        selectionKeysByUser.put(user, key);
    }

    public void stop() {
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
            if (command.getType() == CommandType.REGISTER_COMMAND
                || command.getType() == CommandType.LOGIN_COMMAND) {
                return command;
            }

            throw new UserNotLoggedInException("You have not logged in");
        }

        if (command.getType() == CommandType.LOGIN_COMMAND ||
            command.getType() == CommandType.REGISTER_COMMAND) {
            throw new UserAlreadyLoggedInException("You have already logged in");
        }

        return command;
    }

    public void logIn(User user, SelectionKey key) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        attachStreamingPort(user);
        attach(user, key);
    }

    public void logOut(SelectionKey key) throws UserNotLoggedInException, UserNotRegisteredException {
        User user = (User) key.attachment();

        detachStreamingPort(user);
        selectionKeysByUser.remove(user);
    }

    public void attachStreamingPort(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            if (!isRegistered(user)) {
                throw new UserNotRegisteredException("A User with the Name: " + user.username() + " does not exist");
            }

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

    public void detachStreamingPort(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            if (!isRegistered(user)) {
                throw new UserNotRegisteredException("A User with the Name: " + user.username() + " does not exist");
            }

            if (!isLoggedIn(user)) {
                throw new UserNotLoggedInException("User not logged in");
            }

            ports.remove(streamingPortsByUser.get(user));
            streamingPortsByUser.remove(user);
        }
    }

    private boolean isRegistered(User user) {
        return database.doesUserExist(user);
    }

    public boolean isLoggedIn(User user) {
        return selectionKeysByUser.containsKey(user);
    }

    public long getPort(User user) {
        return streamingPortsByUser.get(user);
    }

    public void addPortStreaming(long port) {
        if (!ports.contains(port)) {
            return;
        }

        currentlyStreamingPorts.add(port);
    }

    public boolean isPortStreaming(long port) {
        return currentlyStreamingPorts.contains(port);
    }

    public void removePortStreaming(long port) {
        if (!isPortStreaming(port)) {
            return;
        }

        currentlyStreamingPorts.remove(port);
    }

    public Database getStorage() {
        return database;
    }

    //    public static void main(String[] args) {
    //        final int port = 6999;
    //        SpotifyServer server = new SpotifyServer(port, new CommandExecutor());
    //        new Thread(server).start();
    //    }

    public static void main(String[] args) {
        SpotifyServer spotifyServer1 = new SpotifyServer(6999, new CommandExecutor());
        new Thread(spotifyServer1).start();
        //
        //        CommandExecutor executor = new CommandExecutor();
        //        User user = new User("filip", "123");
        //
        //        RegisterCommand registerCommand = new RegisterCommand("filip", "123", spotifyServer1);
        //        System.out.println(executor.execute(registerCommand));
        //
        //        LoginCommand loginCommand = new LoginCommand("filip", "123", spotifyServer1);
        //        System.out.println(executor.execute(loginCommand));
        //
        //        LoginCommand loginCommand2 = new LoginCommand("filip", "123", spotifyServer1);
        //        System.out.println(executor.execute(loginCommand2));
        //
        //        CreatePlaylistCommand command = new CreatePlaylistCommand("filipPlaylist", user, spotifyServer1);
        //        System.out.println(executor.execute(command));
        //
        //        AddSongToPlaylistCommand addSongToPlaylistCommand =
        //            new AddSongToPlaylistCommand("Upsurt-Chekai malko", "filipPlaylist", spotifyServer1);
        //        System.out.println(executor.execute(addSongToPlaylistCommand));
        //
        //        AddSongToPlaylistCommand addSongToPlaylistCommand1 =
        //            new AddSongToPlaylistCommand("alabala", "filipPlaylist", spotifyServer1);
        //        System.out.println(executor.execute(addSongToPlaylistCommand1));
        //
        //        AddSongToPlaylistCommand addSongToPlaylistCommand2 =
        //            new AddSongToPlaylistCommand("Upsurt-Chekai malko", "alabala", spotifyServer1);
        //        System.out.println(executor.execute(addSongToPlaylistCommand2));
        //
        //        SearchCommand searchCommand = new SearchCommand("mAlKo", spotifyServer1);
        //        System.out.println(executor.execute(searchCommand));
        //
        //        spotifyServer1.stop();
    }
}