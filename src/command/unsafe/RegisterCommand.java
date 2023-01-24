package command.unsafe;

import command.Command;
import command.exceptions.UnsuccessfulRegistrationException;
import server.SpotifyServer;
import storage.Storage;
import user.exceptions.UserAlreadyExistsException;

public class RegisterCommand extends Command {
    private final String username;
    private final String password;

    public RegisterCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer);
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
}
