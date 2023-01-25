package server;

import database.Database;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotLoggedInException;
import user.exceptions.UserNotRegisteredException;

public interface SpotifyServer extends Runnable {
    Database getDatabase();

    void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException;

    void logOut(User user) throws UserNotLoggedInException, UserNotRegisteredException;

    boolean isLoggedIn(User user);
}
