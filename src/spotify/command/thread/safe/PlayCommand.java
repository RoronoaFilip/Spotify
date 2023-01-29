package spotify.command.thread.safe;

import spotify.command.Command;
import spotify.command.CommandType;
import spotify.database.song.Song;
import spotify.database.user.User;
import spotify.server.SpotifyServerStreamingPermission;
import spotify.server.streamer.SongStreamer;

/**
 * Play Command. Represents a Request from the User for a Song to be played
 * <p>
 * A Valid Play Request looks like this: <br>
 * play "song-name"
 * </p>
 */
public class PlayCommand extends Command {
    public static final String COMMAND = "play";
    private final String fullSongName;
    private final User user;

    public PlayCommand(String fullSongName, User user, SpotifyServerStreamingPermission spotifyServer) {
        super(spotifyServer, CommandType.PLAY_COMMAND);
        this.fullSongName = fullSongName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        SpotifyServerStreamingPermission spotifyServerStreamingPermission =
            (SpotifyServerStreamingPermission) spotifyServer;

        Song toPlay = spotifyServerStreamingPermission.getDatabase().getSongBy(fullSongName);

        long userStreamingPort = spotifyServerStreamingPermission.getPort(user);

        spotifyServerStreamingPermission.isPortLocked(userStreamingPort);

        SongStreamer streamer = new SongStreamer((int) userStreamingPort, toPlay, spotifyServerStreamingPermission);

        Thread thread = new Thread(streamer, "Song Streamer for User: " + user);
        thread.setDaemon(true);
        thread.start();

        return "ok " + toPlay.getAudioFormatString() + " " + userStreamingPort;
    }

    public static PlayCommand of(String line, User user, SpotifyServerStreamingPermission spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        return new PlayCommand(line, user, spotifyServer);
    }

    public String getFullSongName() {
        return fullSongName;
    }

    public User getUser() {
        return user;
    }
}
