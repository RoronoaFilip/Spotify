package command.thread.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServerStreamingPermission;
import server.streamer.SongStreamer;
import song.Song;
import user.User;

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

        spotifyServerStreamingPermission.isPortStreaming(userStreamingPort);

        SongStreamer streamer = new SongStreamer((int) userStreamingPort, toPlay, spotifyServerStreamingPermission);

        Thread thread = new Thread(streamer);
        thread.setDaemon(true);
        thread.start();

        return toPlay.getAudioFormatString() + " " + userStreamingPort;
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