package command.safe;

import command.Command;
import server.SpotifyServer;
import song.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand extends Command {
    private final String[] filters;

    public SearchCommand(String[] filters, SpotifyServer spotifyServer) {
        super(spotifyServer);
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
}
