package command.thread.safe;

import command.Command;
import command.CommandType;
import playlist.Playlist;
import server.SpotifyServer;
import user.User;

import java.util.List;

public class ShowPlaylistCommand extends Command {
    public static final String COMMAND = "show-playlist";
    private final String playlistName;
    private final User owner;

    public ShowPlaylistCommand(String playlistName, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.SHOW_PLAYLIST_COMMAND);
        this.playlistName = playlistName;
        owner = null;
    }

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

        return "Playlist " + playlist.getName() + " by " + playlist.getOwner().username() + ":" +
               System.lineSeparator() + Command.constructMessage(List.copyOf(playlist.getSongs()));
    }

    public static ShowPlaylistCommand of(String line, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        String[] split = split(line, 2);

        if (split.length == 1) {
            return new ShowPlaylistCommand(line, spotifyServer);
        } else if (split.length == 2) {
            String ownerName = split[1];
            String playlistName = split[0];

            User owner = new User(ownerName, "");
            return new ShowPlaylistCommand(playlistName, owner, spotifyServer);
        }

        return null;
    }

    public boolean hasOwner() {
        return owner != null;
    }
}
