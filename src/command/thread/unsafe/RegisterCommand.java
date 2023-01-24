package command.thread.unsafe;

import command.Command;
import command.CommandType;
import command.exceptions.UnsuccessfulRegistrationException;
import server.SpotifyServer;
import user.exceptions.UserAlreadyExistsException;

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
        String message;
        try {
            spotifyServer.getStorage().registerUser(username, password);
            message = SUCCESSFUL_REGISTER;
        } catch (UserAlreadyExistsException e) {
            throw new UnsuccessfulRegistrationException(UNSUCCESSFUL_REGISTER);
        }

        return message;
    }

    public static RegisterCommand of(String line, SpotifyServer spotifyServer) {
        String[] split = split(line);

        if (split.length != 2) {
            return null;
        }

        return new RegisterCommand(split[0], split[1], spotifyServer);
    }
}
