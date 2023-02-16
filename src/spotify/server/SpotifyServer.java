package spotify.server;

import spotify.database.Database;
import spotify.database.user.service.UserService;

/**
 * Represents the lowest Permission in the Spotify Server
 *
 * <p>
 * These Permissions allow access to the Database, logging Users in,
 * logging Users out and checking if a User is logged in
 * </p>
 */
public interface SpotifyServer extends Runnable {
    Database getDatabase();

    UserService getUserService();
}
