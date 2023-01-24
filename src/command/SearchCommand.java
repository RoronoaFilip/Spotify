package command;

import server.SpotifyServer;
import song.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand extends Command {
    private final String filter;

    public SearchCommand(String filter, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.filter = filter;
    }

    @Override
    public String call() {
        List<Song> filteredSongs = new ArrayList<>(spotifyServer.getStorage().filterSongsBasedOn(filter));

        if (filteredSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Songs filtered based on " + filter + ":" + System.lineSeparator() +
               Command.constructMessage(filteredSongs) +
               System.lineSeparator();
    }
}
