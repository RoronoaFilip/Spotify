package spotify.server.command.thread.unsafe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.user.User;
import spotify.server.SpotifyServer;

/**
 * Create Playlist Command. Represents a Request from the User for a Playlist to be shown
 * <p>
 * A Valid Create Playlist Request looks like this: <br>
 * create-playlist "playlist-name" <br>
 * </p>
 */
public class CreatePlaylistCommand extends Command {
    public static final String COMMAND = "create-playlist";
    private final String playlistName;
    private final User owner;

    public CreatePlaylistCommand(String playlistName, User owner, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.CREATE_PLAYLIST_COMMAND);
        this.playlistName = playlistName;
        this.owner = owner;
    }

    @Override
    public String call() throws Exception {
        spotifyServer.getDatabase().createPlaylist(playlistName, owner);

        return "Playlist with Name: " + playlistName + " by " + owner.email() + " was created";
    }

    public static CreatePlaylistCommand of(String line, User owner, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        return new CreatePlaylistCommand(line, owner, spotifyServer);
    }
}
