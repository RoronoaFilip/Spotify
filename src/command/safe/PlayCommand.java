package command.safe;

import command.Command;
import server.SpotifyServer;
import song.Song;
import threads.SongStreamer;
import user.User;

public class PlayCommand extends Command {
    private final String fullSongName;
    private final User user;

    public PlayCommand(String fullSongName, User user, SpotifyServer spotifyServer) {
        super(spotifyServer);
        this.fullSongName = fullSongName;
        this.user = user;
    }


    @Override
    public String call() throws Exception {
        Song toPlay = spotifyServer.getStorage().getSongBy(fullSongName);

        long userStreamingPort = spotifyServer.getPort(user);

        SongStreamer streamer = new SongStreamer((int) userStreamingPort, toPlay);

        //        new Thread(streamer).start();

        return toPlay.getAudioFormatString() + " " + userStreamingPort;
    }
}
