package command;

import song.Song;
import storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class TopSongsCommand extends Command {
    private final boolean all;
    private final int limit;
    private final Storage storage;

    public TopSongsCommand(int limit, Storage storage) {
        this.limit = limit;
        all = false;
        this.storage = storage;
    }

    public TopSongsCommand(boolean all, Storage storage) {
        limit = 0;
        this.all = all;
        this.storage = storage;
    }

    @Override
    public String call() {
        List<Song> topSongs;

        if (all) {
            topSongs = new ArrayList<>(storage.getMostStreamedSongs());
        } else {
            topSongs = new ArrayList<>(storage.getMostStreamedSongs(limit));
        }

        if (topSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Top Songs: " + System.lineSeparator() + Command.constructMessage(topSongs) + System.lineSeparator();
    }
}
