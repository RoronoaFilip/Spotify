package command.safe;

import command.Command;
import server.SpotifyServer;
import song.Song;

import java.util.ArrayList;
import java.util.List;

public class TopSongsCommand extends Command {
    private final boolean all;
    private final int limit;

    public TopSongsCommand(int limit, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.limit = limit;
        all = false;
    }

    public TopSongsCommand(boolean all, SpotifyServer spotifyServer) {
        super(spotifyServer);
        limit = 0;
        this.all = all;
    }

    @Override
    public String call() {
        List<Song> topSongs;

        if (all) {
            topSongs = new ArrayList<>(spotifyServer.getStorage().getMostStreamedSongs());
        } else {
            topSongs = new ArrayList<>(spotifyServer.getStorage().getMostStreamedSongs(limit));
        }

        if (topSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Top Songs: " + System.lineSeparator() + Command.constructMessage(topSongs) + System.lineSeparator();
    }
}
