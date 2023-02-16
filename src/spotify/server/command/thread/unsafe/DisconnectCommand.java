package spotify.server.command.thread.unsafe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.server.SpotifyServer;
import spotify.database.user.User;

/**
 * Disconnect Command. Represents a Request from the User to log out
 * <p>
 * A Valid Disconnect Request looks like this: <br>
 * disconnect
 * </p>
 */
public class DisconnectCommand extends Command {
    public static final String COMMAND = "disconnect";
    private final SpotifyServer spotifyServer;
    private final User user;

    public DisconnectCommand(User user, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.DISCONNECT_COMMAND);
        this.spotifyServer = spotifyServer;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getUserService().logOut(user);

        return SUCCESSFUL_LOGOUT;
    }
}
