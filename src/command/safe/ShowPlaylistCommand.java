package command.safe;

import command.Command;
import playlist.Playlist;
import server.SpotifyServer;

import java.util.List;

public class ShowPlaylistCommand extends Command {
    private final String playlistName;

    public ShowPlaylistCommand(String playlistName, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.playlistName = playlistName;
    }

    @Override
    public String call() throws Exception {
        Playlist playlist = spotifyServer.getStorage().getPlaylistByName(playlistName);


        return "Playlist " + playlist.getName() + ":" + System.lineSeparator() +
               Command.constructMessage(List.copyOf(playlist.getSongs()));
    }
}
