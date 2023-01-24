package command.thread.unsafe;

import command.Command;
import command.CommandType;
import command.exceptions.UnsuccessfulLogInException;
import server.SpotifyServer;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotRegisteredException;

import java.nio.channels.SelectionKey;

public class LoginCommand extends Command {
    private final User user;
    private final SelectionKey key;

    public LoginCommand(String username, String password, SelectionKey key, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.LOGIN_COMMAND);
        user = new User(username, password);
        this.key = key;
    }

    @Override
    public String call() throws Exception {
        String message;
        try {
            spotifyServer.logIn(user, key);
            message = SUCCESSFUL_LOGIN;
        } catch (UserAlreadyLoggedInException e) {
            throw new UnsuccessfulLogInException(UNSUCCESSFUL_LOGIN);
        } catch (UserNotRegisteredException e) {
            throw new UserNotRegisteredException(USER_DOES_NOT_EXIST);
        }

        return message;
    }

    public static LoginCommand of(String line, SelectionKey key, SpotifyServer spotifyServer) {
        String[] split = Command.split(line);

        if (split.length != 2) {
            return null;
        }

        return new LoginCommand(split[0], split[1], key, spotifyServer);
    }

    public User getUser() {
        return user;
    }
}
