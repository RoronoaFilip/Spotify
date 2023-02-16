package spotify.server.command.thread.safe;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.database.song.Song;
import spotify.server.SpotifyServer;
import spotify.server.streamer.SongStreamer;
import spotify.user.User;

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

    public PlayCommand(String fullSongName, User user, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.PLAY_COMMAND);
        this.fullSongName = fullSongName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        Song toPlay = spotifyServer.getDatabase().getSongBy(fullSongName);

        long userStreamingPort = spotifyServer.getUserService().getPort(user);

        spotifyServer.getUserService().isPortLocked(userStreamingPort);

        SongStreamer streamer = new SongStreamer((int) userStreamingPort, toPlay, spotifyServer);

        Thread thread = new Thread(streamer, "Song Streamer for User: " + user);
        thread.setDaemon(true);
        thread.start();

        return "ok " + toPlay.getAudioFormatString() + " " + userStreamingPort;
    }

    public static PlayCommand of(String line, User user, SpotifyServer spotifyServer) {
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
