package command.safe;

import command.Command;
import server.SpotifyServer;

public class StopCommand extends Command {
    public StopCommand(SpotifyServer spotifyServer) {
        super(spotifyServer);
    }


    @Override
    public String call() throws Exception {
        spotifyServer.stop();

        return "Server stopped successfully";
    }
}
