package spotify.server.command.thread.safe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.playlist.Playlist;
import spotify.database.user.User;
import spotify.server.SpotifyServer;

import java.util.List;

/**
 * Show Playlist Command. Represents a Request from the User for a Playlist to be shown
 * <p>
 * A Valid Show Playlist Request looks like this: <br>
 * show-playlist "playlist-name" <br>
 * show-playlist "playlist-name" "owner-email"
 * </p>
 */
public class ShowPlaylistCommand extends Command {
    private static final int PLAYLIST_NAME_INDEX = 0;
    private static final int OWNER_INDEX = 1;
    private static final int COMMAND_MIN_LENGTH = 1;
    private static final int COMMAND_MAX_LENGTH = 2;
    public static final String COMMAND = "show-playlist";
    private final String playlistName;
    private final User owner;

    /**
     * Search for a Playlist only based on Name
     */
    public ShowPlaylistCommand(String playlistName, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.SHOW_PLAYLIST_COMMAND);
        this.playlistName = playlistName;
        owner = null;
    }

    /**
     * Search for a Playlist based on Name and its Owner email
     */
    public ShowPlaylistCommand(String playlistName, User owner, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.SHOW_PLAYLIST_COMMAND);
        this.playlistName = playlistName;
        this.owner = owner;
    }

    @Override
    public String call() throws Exception {
        Playlist playlist;

        if (owner == null) {
            playlist = spotifyServer.getDatabase().getPlaylistByName(playlistName);
        } else {
            playlist = spotifyServer.getDatabase().getPlaylist(playlistName, owner);
        }

        return "Playlist " + playlist.getName() + " by " + playlist.getOwner().email() + ":" + System.lineSeparator() +
               Command.constructMessage(List.copyOf(playlist.getSongs()));
    }

    public static ShowPlaylistCommand of(String line, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        String[] split = split(line, COMMAND_MAX_LENGTH);

        if (split.length == COMMAND_MIN_LENGTH) {

            return new ShowPlaylistCommand(line, spotifyServer);

        } else if (split.length == COMMAND_MAX_LENGTH) {

            String ownerName = split[OWNER_INDEX];
            String playlistName = split[PLAYLIST_NAME_INDEX];

            User owner = new User(ownerName, "");
            return new ShowPlaylistCommand(playlistName, owner, spotifyServer);
        }

        return null;
    }

    public boolean hasOwner() {
        return owner != null;
    }
}
