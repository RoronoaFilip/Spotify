package command;

import server.SpotifyServer;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class Command implements Callable<String> {
    public static final String COMMAND_SPLIT_REGEX = "\\s+";
    protected static final String SUCCESSFUL_LOGIN = "You have logged in successfully";
    protected static final String UNSUCCESSFUL_LOGIN = "Invalid Username or password";
    protected static final String SUCCESSFUL_LOGOUT = "You have logged out";
    protected static final String UNSUCCESSFUL_LOGOUT = "You have not logged in, you can not log out";
    protected static final String SUCCESSFUL_REGISTER = "You have registered successfully";
    protected static final String UNSUCCESSFUL_REGISTER = "This Username has been registered already";
    protected static final String USER_DOES_NOT_EXIST = "Such User does not exist";

    protected SpotifyServer spotifyServer;
    private CommandType type;

    protected Command(SpotifyServer spotifyServer, CommandType type) {
        this.spotifyServer = spotifyServer;
        this.type = type;
    }

    protected static String constructMessage(List<?> objects) {
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < objects.size(); ++i) {
            message.append(i + 1).append(". ").append(objects.get(i)).append(System.lineSeparator());
        }

        return message.toString();
    }

    public CommandType getType() {
        return type;
    }

    protected static String[] split(String line, int limit) {
        String[] split = line.split(COMMAND_SPLIT_REGEX, limit);

        for (String str : split) {
            str.strip();
        }

        return split;
    }

    protected static String[] split(String line) {
        String[] split = line.split(COMMAND_SPLIT_REGEX);

        for (String str : split) {
            str.strip();
        }

        return split;
    }
}
