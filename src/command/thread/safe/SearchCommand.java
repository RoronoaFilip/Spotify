package command.thread.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;
import song.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand extends Command {
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

        return "Found Songs:" + System.lineSeparator() + Command.constructMessage(filteredSongs) +
               System.lineSeparator();
    }

    public static SearchCommand of(String line, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        if (line.equalsIgnoreCase("all")) {
            return new SearchCommand(true, spotifyServer);
        }

        String[] split = split(line);

        if (split.length < 1) {
            return null;
        }

        return new SearchCommand(split, spotifyServer);
    }
}
