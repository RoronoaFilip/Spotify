package command;

import command.executor.CommandExecutor;
import server.SpotifyServer;
import user.User;

import java.io.IOException;

public class CreatePlaylistCommand extends Command {
    private String playlistName;
    private User owner;

    public CreatePlaylistCommand(String playlistName, User owner, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.playlistName = playlistName;
        this.owner = owner;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getStorage().createPlaylist(playlistName, owner);

        String message = "Playlist with Name: " + playlistName + " was created";

        return message;
    }
}
