package command.unsafe;

import command.Command;
import playlist.Playlist;
import playlist.exceptions.InvalidPlaylistOperationException;
import server.SpotifyServer;
import song.Song;
import user.User;

public class AddSongToPlaylistCommand extends Command {
    private final String fullSongName;
    private final String playlistName;
    private final User user;

    public AddSongToPlaylistCommand(String fullSongName, String playlistName, User user, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.fullSongName = fullSongName;
        this.playlistName = playlistName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        Song song = spotifyServer.getStorage().getSongBy(fullSongName);
        Playlist playlist = spotifyServer.getStorage().getPlaylistByName(playlistName);

        if (!playlist.getOwner().equals(user)) {
            throw new InvalidPlaylistOperationException("You are not the Playlist Owner");
        }

        playlist.addSong(song);

        return "Song added successfully";
    }
}
