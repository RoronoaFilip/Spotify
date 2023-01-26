package server;

import database.Database;
import database.user.User;
import database.user.exceptions.UserAlreadyLoggedInException;
import database.user.exceptions.UserNotLoggedInException;
import database.user.exceptions.UserNotRegisteredException;

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

    /**
     * Logs in a User in the System by assigning said User to a Port for Song Streaming
     *
     * @param user the User to be logged in
     * @throws UserAlreadyLoggedInException if the User already has an assigned Port for Song Streaming
     * @throws UserNotRegisteredException   if the User is not registered in the Database
     */
    void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException;

    /**
     * Logs out a User in the System by freeing his Streaming Port
     *
     * @param user the User to be logged out
     * @throws UserNotLoggedInException   if the User does not have an assigned Port for Song Streaming
     * @throws UserNotRegisteredException if the User is not registered in the Database
     */
    void logOut(User user) throws UserNotLoggedInException, UserNotRegisteredException;

    /**
     * Checks if the {@code user} has a Streaming Port assigned to him
     *
     * @param user the User to be checked
     * @return true if the User has an assigned Streaming port, false otherwise
     */
    boolean isLoggedIn(User user);
}
