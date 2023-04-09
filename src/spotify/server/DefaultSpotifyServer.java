package spotify.server;

import spotify.database.Database;
import spotify.database.InMemoryDatabase;
import spotify.database.user.User;
import spotify.database.user.exceptions.UserNotLoggedInException;
import spotify.database.user.exceptions.UserNotRegisteredException;
import spotify.database.user.service.DefaultUserService;
import spotify.database.user.service.UserService;
import spotify.logger.SpotifyLogger;
import spotify.server.command.Command;
import spotify.server.command.executor.CommandExecutor;
import spotify.server.command.factory.CommandFactory;
import spotify.server.command.validator.CommandValidator;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class DefaultSpotifyServer implements SpotifyServerTerminatePermission {
    private static final String LOG_FILE_NAME = "serverLogs.txt";

    private static final long STREAMING_PORT = 7000;

    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;
    private final Database database;
    private final UserService userService;
    private final SpotifyLogger logger;

    private final int port;
    private boolean isServerWorking;

    private final ByteBuffer buffer;
    private Selector selector;

    public DefaultSpotifyServer(int port, CommandExecutor commandExecutor, Database database) {
        this.port = port;
        this.commandExecutor = commandExecutor;
        this.logger = new SpotifyLogger(LOG_FILE_NAME);

        this.userService = new DefaultUserService(STREAMING_PORT, database);

        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

        this.database = database;
        isServerWorking = true;
    }

    @Override
    public void run() {
        final PrintWriter writer = logger.getWriter();
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); database; writer) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);

            while (isServerWorking) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();

                        try {
                            String clientInput = getClientInput(clientChannel);
                            if (clientInput == null) {
                                userService.logOut((User) key.attachment());
                                continue;
                            }

                            logger.logClientInput(clientInput, key);

                            String output;
                            Command cmd = CommandFactory.create(clientInput, (User) key.attachment(), this);
                            try {
                                CommandValidator.checkCommand(cmd, key);

                                output = commandExecutor.execute(cmd);

                                CommandValidator.verifyLogin(cmd, key);
                            } catch (Exception e) {
                                logger.log(e, clientInput, key);
                                output = e.getMessage();
                            }

                            logger.logClientOutput(output, key);
                            writeClientOutput(clientChannel, output);
                        } catch (IOException e) {
                            if (e.getMessage().contains("Connection reset")) {
                                try {
                                    userService.logOut((User) key.attachment());
                                    clientChannel.close();
                                    key.cancel();
                                } catch (UserNotLoggedInException | UserNotRegisteredException ex) {
                                    //
                                }
                            }
                            System.out.println("Error occurred while processing client request: " + e.getMessage());
                        } catch (UserNotLoggedInException | UserNotRegisteredException e) {
                            // ignore - user has disconnected
                        }
                    } else if (key.isAcceptable()) {
                        accept(selector, key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("failed to start server");
        }
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

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    public static void main(String[] args) {
        final int port = 6999;

        DefaultSpotifyServer spotifyServer1 = new DefaultSpotifyServer(port, new CommandExecutor(),
            new InMemoryDatabase(Database.SONGS_FOLDER_DEFAULT, Database.DATABASE_FOLDER_DEFAULT,
                Database.USERS_FILE_NAME_DEFAULT, Database.PLAYLISTS_FILE_NAME_DEFAULT));

        new Thread(spotifyServer1, "Spotify Server Thread").start();
    }
}