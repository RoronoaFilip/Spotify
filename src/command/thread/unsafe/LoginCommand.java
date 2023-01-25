package command.thread.unsafe;

import command.Command;
import command.CommandType;
import command.exceptions.UnsuccessfulLogInException;
import server.SpotifyServer;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotRegisteredException;

public class LoginCommand extends Command {
    private final User user;
    private boolean successful = false;

    public LoginCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.LOGIN_COMMAND);
        user = new User(username, password);
    }

    @Override
    public String call() throws Exception {
        String message;
        try {
            spotifyServer.logIn(user);
            message = SUCCESSFUL_LOGIN;
            successful = true;
        } catch (UserAlreadyLoggedInException e) {
            throw new UnsuccessfulLogInException(UNSUCCESSFUL_LOGIN);
        } catch (UserNotRegisteredException e) {
            throw new UserNotRegisteredException(USER_DOES_NOT_EXIST);
        }

        return message;
    }

    public static LoginCommand of(String line, SpotifyServer spotifyServer) {
        String[] split = Command.split(line);

        if (split.length != 2) {
            return null;
        }

        return new LoginCommand(split[0], split[1], spotifyServer);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public User getUser() {
        return user;
    }
}
