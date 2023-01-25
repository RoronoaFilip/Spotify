package command.thread.unsafe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;
import user.User;

public class LoginCommand extends Command {
    public static final String COMMAND = "login";
    private final User user;
    private boolean successful = false;

    public LoginCommand(String username, String password, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.LOGIN_COMMAND);
        user = new User(username, password);
    }

    @Override
    public String call() throws Exception {
        spotifyServer.logIn(user);

        successful = true;
        return SUCCESSFUL_LOGIN;
    }

    public static LoginCommand of(String line, SpotifyServer spotifyServer) {
        String[] split = Command.split(line);

        if (split.length != 2) {
            return null;
        }

        return new LoginCommand(split[0], split[1], spotifyServer);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public User getUser() {
        return user;
    }
}
