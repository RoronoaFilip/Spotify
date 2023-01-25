package command.thread.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServerTerminatePermission;

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
