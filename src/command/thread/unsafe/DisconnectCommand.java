package command.thread.unsafe;

import command.Command;
import command.CommandType;
import command.exceptions.UnsuccessfulLogOutException;
import server.SpotifyServer;
import user.User;
import user.exceptions.UserNotLoggedInException;
import user.exceptions.UserNotRegisteredException;

public class DisconnectCommand extends Command {
    private final SpotifyServer spotifyServer;
    private final User user;

    public DisconnectCommand(User user, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.DISCONNECT_COMMAND);
        this.spotifyServer = spotifyServer;
        this.user = user;
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
