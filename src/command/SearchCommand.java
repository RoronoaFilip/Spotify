package command;

import song.Song;
import storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand extends Command {
    private final String filter;
    private final Storage storage;

    public SearchCommand(String filter, Storage storage) {
        this.filter = filter;
        this.storage = storage;
    }

    @Override
    public String call() {
        List<Song> filteredSongs = new ArrayList<>(storage.filterSongsBasedOn(filter));

        if (filteredSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Songs filtered based on " + filter + ":" + System.lineSeparator() +
               Command.constructMessage(filteredSongs) +
               System.lineSeparator();
    }
}
