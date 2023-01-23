package command;

import command.exceptions.UnsuccessfulLogInException;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotRegisteredException;
import server.SpotifyServer;
import storage.InMemoryStorage;
import storage.Storage;
import user.User;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginCommand extends Command {
    private final String username;
    private final String password;
    private final SpotifyServer spotifyServer;

    public LoginCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer.getStorage());
        this.username = username;
        this.password = password;
        this.spotifyServer = spotifyServer;
    }

    @Override
    public String call() throws Exception {
        User user = new User(username, password);

        String message = null;
        try {
            spotifyServer.logIn(user);
            message = SUCCESSFUL_LOGIN;
        } catch (UserAlreadyLoggedInException e) {
            throw new UnsuccessfulLogInException(UNSUCCESSFUL_LOGIN);
        } catch (UserNotRegisteredException e) {
            throw new UserNotRegisteredException(USER_DOES_NOT_EXIST);
        }

        return message;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SpotifyServer server = new SpotifyServer(7777, null);
        Storage storage1 = server.getStorage();
        ExecutorService executor = Executors.newCachedThreadPool();
        Set<Future<String>> set = new HashSet<>();

        for (int i = 0; i < 100; ++i) {
            set.add(executor.submit(new LoginCommand("filip", "123", server)));
        }

        for (Future<String> future : set) {
            System.out.println(future.get());
        }

        InMemoryStorage memoryStorage = (InMemoryStorage) storage1;
        System.out.println(memoryStorage.getUsers().size());
        executor.shutdown();
    }
}
