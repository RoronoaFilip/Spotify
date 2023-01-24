package server;

import command.AddSongToPlaylistCommand;
import command.CreatePlaylistCommand;
import command.LoginCommand;
import command.RegisterCommand;
import command.SearchCommand;
import command.creator.CommandCreator;
import command.executor.CommandExecutor;
import storage.InMemoryStorage;
import storage.Storage;
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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class SpotifyServer implements Runnable {
    private static final long STREAMING_PORT = 7000;

    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;
    private final Storage storage;

    private final int port;
    private boolean isServerWorking;

    private final ByteBuffer buffer;
    private Selector selector;

    private final Map<User, Long> currentSession;
    private final TreeSet<Long> ports;
    private final Object currentSessionLock = new Object();

    public SpotifyServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;

        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

        currentSession = new HashMap<>();
        ports = new TreeSet<>();

        storage = new InMemoryStorage();
        isServerWorking = true;
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); storage) {
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

                            if (clientInput.equals("stop")) {
                                stop();
                            }

                            String output = commandExecutor.execute(CommandCreator.create(clientInput));
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

    public void stop() {
        this.isServerWorking = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }

        try {
            storage.close();
        } catch (IOException e) {
            System.out.println("Error occurred while closing Storage");
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

    public void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            isRegistered(user);

            try {
                isLoggedIn(user);
            } catch (UserNotLoggedInException e) {
                // Ignore - that is what we want
            }

            if (currentSession.isEmpty()) {
                currentSession.put(user, STREAMING_PORT);
                ports.add(STREAMING_PORT);
                return;
            }

            long nextKey = ports.last() + 1;
            currentSession.put(user, nextKey);
            ports.add(nextKey);
        }
    }

    private void logOut(User user) throws UserNotLoggedInException {
        synchronized (currentSessionLock) {
            try {
                isLoggedIn(user);
            } catch (UserAlreadyLoggedInException e) {
                ports.remove(currentSession.get(user));
                currentSession.remove(user);
            }
        }
    }

    private boolean isRegistered(User user) throws UserNotRegisteredException {
        return storage.doesUserExist(user);
    }

    private boolean isLoggedIn(User user) throws UserNotLoggedInException, UserAlreadyLoggedInException {
        if (!currentSession.containsKey(user)) {
            throw new UserNotLoggedInException(
                "A User with Username " + user.username() + " and Password " + user.password() +
                " has not logged in");
        }

        throw new UserAlreadyLoggedInException(
            "A User with Username " + user.username() + " and Password " + user.password() +
            " has already logged in");
    }

    public long getPort(User user) {
        return currentSession.get(user);
    }

    public Storage getStorage() {
        return storage;
    }

    //    public static void main(String[] args) {
    //        final int port = 6999;
    //        SpotifyServer server = new SpotifyServer(port, new CommandExecutor());
    //        new Thread(server).start();
    //    }

    public static void main(String[] args) {
        SpotifyServer spotifyServer1 = new SpotifyServer(6999, new CommandExecutor());

        CommandExecutor executor = new CommandExecutor();
        User user = new User("filip", "123");

        RegisterCommand registerCommand = new RegisterCommand("filip", "123", spotifyServer1);
        System.out.println(executor.execute(registerCommand));

        LoginCommand loginCommand = new LoginCommand("filip", "123", spotifyServer1);
        System.out.println(executor.execute(loginCommand));

        LoginCommand loginCommand2 = new LoginCommand("filip", "123", spotifyServer1);
        System.out.println(executor.execute(loginCommand2));

        CreatePlaylistCommand command = new CreatePlaylistCommand("filipPlaylist", user, spotifyServer1);
        System.out.println(executor.execute(command));

        AddSongToPlaylistCommand addSongToPlaylistCommand =
            new AddSongToPlaylistCommand("Upsurt-Chekai malko", "filipPlaylist", spotifyServer1);
        System.out.println(executor.execute(addSongToPlaylistCommand));

        AddSongToPlaylistCommand addSongToPlaylistCommand1 =
            new AddSongToPlaylistCommand("alabala", "filipPlaylist", spotifyServer1);
        System.out.println(executor.execute(addSongToPlaylistCommand1));

        AddSongToPlaylistCommand addSongToPlaylistCommand2 =
            new AddSongToPlaylistCommand("Upsurt-Chekai malko", "alabala", spotifyServer1);
        System.out.println(executor.execute(addSongToPlaylistCommand2));

        SearchCommand searchCommand = new SearchCommand("mAlKo", spotifyServer1);
        System.out.println(executor.execute(searchCommand));

        spotifyServer1.stop();
    }
}
