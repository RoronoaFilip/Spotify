package command.thread.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;
import song.Song;

import java.util.ArrayList;
import java.util.List;

public class TopSongsCommand extends Command {
    public static final String COMMAND = "top";
    private final boolean all;
    private final int limit;

    public TopSongsCommand(int limit, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.TOP_SONGS_COMMAND);
        this.limit = limit;
        all = false;
    }

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

        return "Top Songs: " + System.lineSeparator() + Command.constructMessage(topSongs) + System.lineSeparator();
    }

    public static TopSongsCommand of(String line, SpotifyServer spotifyServer) {
        if (line.equalsIgnoreCase("all")) {
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
