package command.thread.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;

public class TerminateCommand extends Command {
    public TerminateCommand(SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.STOP_COMMAND);
    }

    @Override
    public String call() throws Exception {
        spotifyServer.stop();

        return "Server stopped successfully";
    }
}
