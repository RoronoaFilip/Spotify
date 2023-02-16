package spotify.server.command.thread.unsafe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.server.SpotifyServer;

/**
 * Register Command. Represents a Request from the User to register into the System
 * <p>
 * A Valid Register Request looks like this: <br>
 * register "username" "password"
 * </p>
 */
public class RegisterCommand extends Command {
    private static final int COMMAND_MAX_LENGTH = 2;
    private static final int EMAIL_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;
    public static final String COMMAND = "register";
    private final String email;
    private final String password;

    public RegisterCommand(String email, String password, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.REGISTER_COMMAND);
        this.email = email;
        this.password = password;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getDatabase().registerUser(email, password);

        return SUCCESSFUL_REGISTER;
    }

    public static RegisterCommand of(String line, SpotifyServer spotifyServer) {
        String[] split = split(line);

        if (split.length != COMMAND_MAX_LENGTH) {
            return null;
        }

        String email = split[EMAIL_INDEX];
        String password = split[PASSWORD_INDEX];

        return new RegisterCommand(email, password, spotifyServer);
    }
}
