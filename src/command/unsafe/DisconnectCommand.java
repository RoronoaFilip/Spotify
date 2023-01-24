package command.unsafe;

import command.Command;
import command.exceptions.UnsuccessfulLogOutException;
import server.SpotifyServer;
import user.User;
import user.exceptions.UserNotLoggedInException;
import user.exceptions.UserNotRegisteredException;

public class DisconnectCommand extends Command {
    private final User user;
    private final SpotifyServer spotifyServer;

    public DisconnectCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer);
        user = new User(username, password);
        this.spotifyServer = spotifyServer;
    }

    @Override
    public String call() throws Exception {
        String message;
        try {
            spotifyServer.logOut(user);
            message = SUCCESSFUL_LOGOUT;
        } catch (UserNotRegisteredException e) {
            throw new UserNotRegisteredException(USER_DOES_NOT_EXIST);
        } catch (UserNotLoggedInException e) {
            throw new UnsuccessfulLogOutException(UNSUCCESSFUL_LOGOUT);
        }

        return message;
    }
}
