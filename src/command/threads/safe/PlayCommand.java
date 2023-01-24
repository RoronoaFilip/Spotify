package command.threads.safe;

import command.Command;
import command.CommandType;
import server.SpotifyServer;
import server.exceptions.PortCurrentlyStreamingException;
import server.threads.SongStreamer;
import song.Song;
import user.User;

public class PlayCommand extends Command {
    private final String fullSongName;
    private final User user;

    public PlayCommand(String fullSongName, User user, SpotifyServer spotifyServer) {
        super(spotifyServer, CommandType.PLAY_COMMAND);
        this.fullSongName = fullSongName;
        this.user = user;
    }


    @Override
    public String call() throws Exception {
        Song toPlay = spotifyServer.getStorage().getSongBy(fullSongName);

        long userStreamingPort = spotifyServer.getPort(user);

        if (spotifyServer.isPortStreaming(userStreamingPort)) {
            throw new PortCurrentlyStreamingException(
                "You are currently listening to a Song. Stop the current one and than try again");
        }

        SongStreamer streamer = new SongStreamer((int) userStreamingPort, toPlay, spotifyServer);

        new Thread(streamer).start();

        toPlay.stream();

        return toPlay.getAudioFormatString() + " " + userStreamingPort;
    }

    public static PlayCommand of(String line, User user, SpotifyServer spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        return new PlayCommand(line, user, spotifyServer);
    }
}
