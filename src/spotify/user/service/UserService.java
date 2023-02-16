package spotify.user.service;

import spotify.server.command.Command;
import spotify.server.exceptions.PortCurrentlyStreamingException;
import spotify.user.User;
import spotify.user.exceptions.UserAlreadyLoggedInException;
import spotify.user.exceptions.UserNotLoggedInException;
import spotify.user.exceptions.UserNotRegisteredException;

import java.nio.channels.SelectionKey;

public interface UserService {
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
