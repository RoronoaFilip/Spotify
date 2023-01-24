package command.unsafe;

import command.Command;
import server.SpotifyServer;
import user.User;

public class CreatePlaylistCommand extends Command {
    private final String playlistName;
    private final User owner;

    public CreatePlaylistCommand(String playlistName, User owner, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.playlistName = playlistName;
        this.owner = owner;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getStorage().createPlaylist(playlistName, owner);

        return "Playlist with Name: " + playlistName + " was created";
    }
}
