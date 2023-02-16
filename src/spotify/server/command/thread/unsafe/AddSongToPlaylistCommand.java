package spotify.server.command.thread.unsafe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.playlist.Playlist;
import spotify.database.playlist.exceptions.NoSuchPlaylistException;
import spotify.database.song.Song;
import spotify.database.user.User;
import spotify.server.SpotifyServer;

/**
 * Add Song to Playlist Command. Represents a Request from
 * the User for a Song to be added to one of his Playlist
 * <p>
 * A Valid Add Song to Playlist Request looks like this: <br>
 * add-song-to "playlist-name" <br>
 * </p>
 */
public class AddSongToPlaylistCommand extends Command {
    private static final int COMMAND_MAX_LENGTH = 2;
    private static final int PLAYLIST_NAME_INDEX = 0;
    private static final int SONG_NAME_INDEX = 1;

    public static final String COMMAND = "add-song-to";
    private final String fullSongName;
    private final String playlistName;
    private final User user;

    public AddSongToPlaylistCommand(String fullSongName, String playlistName, User user, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.ADD_SONG_TO_PLAYLIST_COMMAND);
        this.fullSongName = fullSongName;
        this.playlistName = playlistName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        Song song = spotifyServer.getDatabase().getSongBy(fullSongName);

        try {
            Playlist playlist = spotifyServer.getDatabase().getPlaylist(playlistName, user);
            playlist.addSong(song);
        } catch (NoSuchPlaylistException e) {
            throw new NoSuchPlaylistException("You do not own a Playlist with the Name: " + playlistName);
        }

        return "Song added successfully";
    }

    public static AddSongToPlaylistCommand of(String line, User user, SpotifyServer spotifyServer) {
        String[] split = split(line, COMMAND_MAX_LENGTH);

        if (split.length != COMMAND_MAX_LENGTH) {
            return null;
        }

        String fullSongName = split[SONG_NAME_INDEX];
        String playlistName = split[PLAYLIST_NAME_INDEX];

        return new AddSongToPlaylistCommand(fullSongName, playlistName, user, spotifyServer);
    }
}
