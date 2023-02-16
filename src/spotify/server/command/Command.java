package spotify.server.command;

import spotify.database.song.Song;
import spotify.server.SpotifyServer;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Threads that Represent what a User can do in the System
 * <p>
 * Most of the Commands use the corresponding Methods from the Server's Database
 * </p>
 */
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
    private final CommandType type;

    protected Command(SpotifyServer spotifyServer, CommandType type) {
        this.spotifyServer = spotifyServer;
        this.type = type;
    }

    protected static String constructMessage(List<Song> songs) {
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < songs.size(); ++i) {
            message.append(i + 1).append(". ").append(songs.get(i)).append(" -> Streams: ")
                .append(songs.get(i).getStreams()).append(System.lineSeparator());
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
