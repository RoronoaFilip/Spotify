package spotify.server.command.thread.safe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.song.Song;
import spotify.server.SpotifyServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Search Command. Represents a Search Request from the User
 * <p>
 * A Valid Search Request looks like this: <br>
 * search "filter1 filter2 .. filterN"<br>
 * search all -> returns all Songs in the Database
 * </p>
 */
public class SearchCommand extends Command {
    private static final int COMMAND_MIN_LENGTH = 1;
    public static final String ALL_COMMAND = "all";
    public static final String COMMAND = "search";
    private final String[] filters;
    private final boolean all;

    public SearchCommand(String[] filters, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.SEARCH_COMMAND);
        this.filters = filters;
        all = false;
    }

    public SearchCommand(boolean all, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.SEARCH_COMMAND);
        this.filters = null;
        this.all = all;
    }

    @Override
    public String call() {
        List<Song> filteredSongs;

        if (all) {
            filteredSongs = new ArrayList<>(spotifyServer.getDatabase().getAllSongs());
        } else {
            filteredSongs = new ArrayList<>(spotifyServer.getDatabase().filterSongsBasedOn(filters));
        }

        if (filteredSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Found Songs:" + System.lineSeparator() + Command.constructMessage(filteredSongs);
    }

    public static SearchCommand of(String line, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        if (line.equalsIgnoreCase(ALL_COMMAND)) {
            return new SearchCommand(true, spotifyServer);
        }

        String[] split = split(line);

        if (split.length < COMMAND_MIN_LENGTH) {
            return null;
        }

        return new SearchCommand(split, spotifyServer);
    }

    public boolean isAll() {
        return all;
    }
}
