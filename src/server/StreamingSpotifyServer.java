package server;

import server.exceptions.PortCurrentlyStreamingException;
import user.User;

public interface StreamingSpotifyServer extends SpotifyServer {
    void addPortStreaming(long port);

    void isPortStreaming(long port) throws PortCurrentlyStreamingException;

    void removePortStreaming(long port);

    long getPort(User user);
}
