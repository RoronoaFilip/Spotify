package command.thread.safe;

import command.Command;
import command.CommandType;
import server.StreamingSpotifyServer;
import server.streamer.SongStreamer;
import song.Song;
import user.User;

public class PlayCommand extends Command {
    private final String fullSongName;
    private final User user;

    public PlayCommand(String fullSongName, User user, StreamingSpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.PLAY_COMMAND);
        this.fullSongName = fullSongName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        StreamingSpotifyServer streamingSpotifyServer = (StreamingSpotifyServer) spotifyServer;

        Song toPlay = streamingSpotifyServer.getDatabase().getSongBy(fullSongName);

        long userStreamingPort = streamingSpotifyServer.getPort(user);

        streamingSpotifyServer.isPortStreaming(userStreamingPort);

        SongStreamer streamer = new SongStreamer((int) userStreamingPort, toPlay, streamingSpotifyServer);

        Thread thread = new Thread(streamer);
        thread.setDaemon(true);
        thread.start();

        return toPlay.getAudioFormatString() + " " + userStreamingPort;
    }

    public static PlayCommand of(String line, User user, StreamingSpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        return new PlayCommand(line, user, spotifyServer);
    }
}
