package command.unsafe;

import command.Command;
import command.exceptions.UnsuccessfulLogInException;
import server.SpotifyServer;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotRegisteredException;

public class LoginCommand extends Command {
    private final User user;

    public LoginCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer);
        user = new User(username, password);
    }

    @Override
    public String call() throws Exception {
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
