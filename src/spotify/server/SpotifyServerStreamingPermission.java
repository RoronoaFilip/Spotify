package spotify.server;

import spotify.database.user.User;
import spotify.server.exceptions.PortCurrentlyStreamingException;

/**
 * Represents 2nd Level of Permissions in the Spotify Server
 *
 * <p>
 * These Permissions allow all Permissions included in the Lowest Level Permissions
 * including access to User Streaming Ports, including marking them as Streaming and as Free
 * </p>
 */
public interface SpotifyServerStreamingPermission extends SpotifyServer {
    /**
     * Marks a Port as Streaming
     *
     * @param port the Port to be marked as Streaming
     */
    void lockPort(long port);

    /**
     * Checks if the {@code port} is marked as Streaming
     *
     * @param port the Port to be checked
     * @throws PortCurrentlyStreamingException if the {@code port} is marked as Streaming
     */
    void isPortLocked(long port) throws PortCurrentlyStreamingException;

    /**
     * Marks a Port as Free
     *
     * @param port the Port to be marked as Free
     */
    void freePort(long port);

    /**
     * Gets the User Streaming Port
     *
     * @param user the User whose Port is searched
     * @return the User Streaming Port if said User has been assigned to a Streaming Port, -1 otherwise
     */
    long getPort(User user);
}
