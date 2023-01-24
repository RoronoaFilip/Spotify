package command.thread.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;
import song.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand extends Command {
    private final String[] filters;

    public SearchCommand(String[] filters, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.SEARCH_COMMAND);
        this.filters = filters;
    }

    @Override
    public String call() {
        List<Song> filteredSongs = new ArrayList<>(spotifyServer.getStorage().filterSongsBasedOn(filters));

        if (filteredSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Filtered Songs:" + System.lineSeparator() +
               Command.constructMessage(filteredSongs) +
               System.lineSeparator();
    }

    public static SearchCommand of(String line, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        String[] split = split(line);

        if (split.length < 1) {
            return null;
        }

        return new SearchCommand(split, spotifyServer);
    }
}
