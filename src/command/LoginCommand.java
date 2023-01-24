package command;

import command.exceptions.UnsuccessfulLogInException;
import server.SpotifyServer;
import storage.InMemoryStorage;
import storage.Storage;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotRegisteredException;

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
        super(spotifyServer);
        this.username = username;
        this.password = password;
        this.spotifyServer = spotifyServer;
    }

    @Override
    public String call() throws Exception {
        User user = new User(username, password);

        String message;
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
}
