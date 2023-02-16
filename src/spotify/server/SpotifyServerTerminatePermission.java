package spotify.server;

/**
 * Represents 3rd Level of Permissions in the Spotify Server
 *
 * <p>
 * These Permissions allow all Permissions included in the Streaming Permission
 * including terminating the Server
 * </p>
 */
public interface SpotifyServerTerminatePermission extends SpotifyServer {
    /**
     * Marks the Server as not Working and waking up any Selectors that have status Waiting
     */
    void terminate();
}
