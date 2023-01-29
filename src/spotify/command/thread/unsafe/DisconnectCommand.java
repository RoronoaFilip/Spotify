package spotify.command.thread.unsafe;

import spotify.command.Command;
import spotify.command.CommandType;
import spotify.database.user.User;
import spotify.server.SpotifyServer;

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
        spotifyServer.logOut(user);

        return SUCCESSFUL_LOGOUT;
    }
}
