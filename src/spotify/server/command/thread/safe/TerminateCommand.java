package spotify.server.command.thread.safe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.server.SpotifyServerTerminatePermission;

/**
 * Terminate Command. Represents a Request from the User for the Server to Stop Working
 * <p>
 * A Valid Terminate Request looks like this: <br>
 * terminate
 * </p>
 */
public class TerminateCommand extends Command {
    public static final String COMMAND = "terminate";

    public TerminateCommand(SpotifyServerTerminatePermission spotifyServer) {
        super(spotifyServer, CommandType.TERMINATE_COMMAND);
    }

    @Override
    public String call() throws Exception {
        SpotifyServerTerminatePermission spotifyServerTerminatePermission =
            (SpotifyServerTerminatePermission) spotifyServer;

        spotifyServerTerminatePermission.terminate();

        return "Server stopped successfully";
    }
}
