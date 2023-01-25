package command.thread.unsafe;

import command.Command;
import command.CommandType;
import playlist.Playlist;
import playlist.exceptions.InvalidPlaylistOperationException;
import playlist.exceptions.NoSuchPlaylistException;
import server.SpotifyServer;
import song.Song;
import user.User;

public class AddSongToPlaylistCommand extends Command {
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
            throw new InvalidPlaylistOperationException("You do not own a Playlist with the Name: " + playlistName);
        }

        return "Song added successfully";
    }

    public static AddSongToPlaylistCommand of(String line, User user, SpotifyServer spotifyServer) {
        String[] split = split(line, 2);

        if (split.length != 2) {
            return null;
        }

        return new AddSongToPlaylistCommand(split[1], split[0], user, spotifyServer);
    }
}
