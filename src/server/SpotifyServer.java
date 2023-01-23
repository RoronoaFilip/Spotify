package server;

import command.executor.CommandExecutor;
import exceptions.UserAlreadyLoggedInException;
import exceptions.UserNotRegisteredException;
import storage.InMemoryStorage;
import storage.Storage;
import user.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.TreeMap;

public class SpotifyServer implements Runnable {
    private static final long STREAMING_PORT = 7000;
    private static final long SERVER_PORT = 6999;

    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;
    private final Storage storage;

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    private final TreeMap<Long, User> currentSession;
    private final Object currentSessionLock = new Object();

    public SpotifyServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;

        storage = new InMemoryStorage();
        currentSession = new TreeMap<>();
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
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

                            //String output = commandExecutor.execute(CommandCreator.newCommand(clientInput));
                            //writeClientOutput(clientChannel, output);

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
        if (selector.isOpen()) {
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

    public void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkLoggedIn(user);
            checkRegistered(user);

            if (currentSession.isEmpty()) {
                currentSession.put(STREAMING_PORT, user);
                return;
            }

            long nextKey = currentSession.lastKey() + 1;
            currentSession.put(nextKey, user);
        }
    }

    private void checkRegistered(User user) throws UserNotRegisteredException {
        if (!storage.doesUserExist(user)) {
            throw new UserNotRegisteredException(
                "A User with Username " + user.username() + " and Password " + user.password() +
                " has not been registered");
        }
    }

    private void checkLoggedIn(User user) throws UserAlreadyLoggedInException {
        if (currentSession.containsValue(user)) {
            throw new UserAlreadyLoggedInException(
                "A User with Username " + user.username() + " and Password " + user.password() +
                " has registered already");
        }
    }

    public long getPort(User user) throws UserAlreadyLoggedInException {
        checkLoggedIn(user);

        for (Long key : currentSession.keySet()) {
            if (currentSession.get(key).equals(user)) {
                return key;
            }
        }

        return STREAMING_PORT;
    }

    public Storage getStorage() {
        return storage;
    }

    public static void main(String[] args) {
        SpotifyServer server = new SpotifyServer(7777, new CommandExecutor(new InMemoryStorage()));
        new Thread(server).start();
    }
}
