package command.thread.unsafe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;

public class RegisterCommand extends Command {
    private final String username;
    private final String password;

    public RegisterCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.REGISTER_COMMAND);
        this.username = username;
        this.password = password;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getDatabase().registerUser(username, password);

        return SUCCESSFUL_REGISTER;
    }

    public static RegisterCommand of(String line, SpotifyServer spotifyServer) {
        String[] split = split(line);

        if (split.length != 2) {
            return null;
        }

        return new RegisterCommand(split[0], split[1], spotifyServer);
    }
}
