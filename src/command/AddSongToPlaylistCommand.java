package command;

import playlist.Playlist;
import server.SpotifyServer;
import song.Song;

public class AddSongToPlaylistCommand extends Command {

    private final String fullSongName;
    private final String playlistName;

    public AddSongToPlaylistCommand(String fullSongName, String playlistName, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.fullSongName = fullSongName;
        this.playlistName = playlistName;
    }

    @Override
    public String call() throws Exception {
        Song song = spotifyServer.getStorage().getSongBy(fullSongName);
        Playlist playlist = spotifyServer.getStorage().getPlaylistByName(playlistName);

        playlist.addSong(song);

        return "Song added successfully";
    }
}
