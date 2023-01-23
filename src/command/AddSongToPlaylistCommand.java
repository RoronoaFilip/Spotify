package command;

import playlist.Playlist;
import song.Song;
import storage.Storage;

public class AddSongToPlaylistCommand extends Command {

    private final String fullSongName;
    private final String playlistName;

    public AddSongToPlaylistCommand(String fullSongName, String playlistName, Storage storage) {
        super(storage);
        this.fullSongName = fullSongName;
        this.playlistName = playlistName;
    }

    @Override
    public String call() throws Exception {
        Song song = storage.getSongBy(fullSongName);
        Playlist playlist = storage.getPlaylistByName(playlistName);

        playlist.addSong(song);

        return "Song added successfully";
    }
}
