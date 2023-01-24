package command.thread.unsafe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;
import user.User;

public class CreatePlaylistCommand extends Command {
    private final String playlistName;
    private final User owner;

    public CreatePlaylistCommand(String playlistName, User owner, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.CREATE_PLAYLIST_COMMAND);
        this.playlistName = playlistName;
        this.owner = owner;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getStorage().createPlaylist(playlistName, owner);

        return "Playlist with Name: " + playlistName + " by " + owner.username() + " was created";
    }

    public static CreatePlaylistCommand of(String line, User owner, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        return new CreatePlaylistCommand(line, owner, spotifyServer);
    }
}
