package spotify.server.command.thread.unsafe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.user.User;
import spotify.server.SpotifyServer;

/**
 * Login Command. Represents a Request from the User to login into the System
 * <p>
 * A Valid Login Request looks like this: <br>
 * login "username" "password"
 * </p>
 */
public class LoginCommand extends Command {
    private static final int COMMAND_MAX_LENGTH = 2;
    private static final int EMAIL_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;
    public static final String COMMAND = "login";
    private final User user;
    private boolean successful = false;

    public LoginCommand(String email, String password, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.LOGIN_COMMAND);
        user = new User(email, password);
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getUserService().logIn(user);

        successful = true;
        return SUCCESSFUL_LOGIN;
    }

    public static LoginCommand of(String line, SpotifyServer spotifyServer) {
        String[] split = Command.split(line);

        if (split.length != COMMAND_MAX_LENGTH) {
            return null;
        }

        String email = split[EMAIL_INDEX];
        String password = split[PASSWORD_INDEX];

        return new LoginCommand(email, password, spotifyServer);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public User getUser() {
        return user;
    }
}
