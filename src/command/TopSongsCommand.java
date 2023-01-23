package command;

import song.Song;
import storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class TopSongsCommand extends Command {
    private final boolean all;
    private final int limit;

    public TopSongsCommand(int limit, Storage storage) {
        super(storage);
        this.limit = limit;
        all = false;
    }

    public TopSongsCommand(boolean all, Storage storage) {
        super(storage);
        limit = 0;
        this.all = all;
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
