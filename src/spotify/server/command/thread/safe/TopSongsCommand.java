package spotify.server.command.thread.safe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.song.Song;
import spotify.server.SpotifyServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Top Songs Command. Represents a Request from the User for a Top Streamed Songs to be shown
 * <p>
 * A Valid Top Songs Request looks like this: <br>
 * top "n" -> shows top n Songs sorted by Streams<br>
 * top all -> shows all Songs sorted by Streams
 * </p>
 */
public class TopSongsCommand extends Command {
    public static final String COMMAND = "top";
    private static final String ALL_COMMAND = "all";
    private final boolean all;
    private final int limit;

    /**
     * Top N Command
     */
    public TopSongsCommand(int limit, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.TOP_SONGS_COMMAND);
        this.limit = limit;
        all = false;
    }

    /**
     * Top all Command
     */
    public TopSongsCommand(boolean all, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.TOP_SONGS_COMMAND);
        limit = 0;
        this.all = all;
    }

    @Override
    public String call() {
        List<Song> topSongs;

        if (all) {
            topSongs = new ArrayList<>(spotifyServer.getDatabase().getMostStreamedSongs());
        } else {
            topSongs = new ArrayList<>(spotifyServer.getDatabase().getMostStreamedSongs(limit));
        }

        if (topSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Top Songs: " + System.lineSeparator() + Command.constructMessage(topSongs);
    }

    public static TopSongsCommand of(String line, SpotifyServer spotifyServer) {
        if (line.equalsIgnoreCase(ALL_COMMAND)) {
            return new TopSongsCommand(true, spotifyServer);
        }

        try {
            int limit = Integer.parseInt(line);
            return new TopSongsCommand(limit, spotifyServer);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isAll() {
        return all;
    }
}
