package spotify.database.user.service;

import spotify.database.Database;
import spotify.database.user.User;
import spotify.database.user.exceptions.UserAlreadyLoggedInException;
import spotify.server.exceptions.PortCurrentlyStreamingException;
import spotify.database.user.exceptions.UserNotLoggedInException;
import spotify.database.user.exceptions.UserNotRegisteredException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DefaultUserService implements UserService {
    private final long initialStreamingPort;
    private final Database database;

    private final Map<User, Long> streamingPortsByUser;
    private final Set<Long> currentlyStreamingPorts;
    private final TreeSet<Long> ports;
    private final Object currentSessionLock = new Object();

    public DefaultUserService(long initialStreamingPort, Database database) {
        this.initialStreamingPort = initialStreamingPort;
        this.database = database;

        streamingPortsByUser = new HashMap<>();
        currentlyStreamingPorts = new HashSet<>();
        ports = new TreeSet<>();
    }

    @Override
    public void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        attachStreamingPort(user);
    }

    @Override
    public void logOut(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        detachStreamingPort(user);
    }

    private void attachStreamingPort(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkRegistered(user);

            if (isLoggedIn(user)) {
                throw new UserAlreadyLoggedInException("User already logged in");
            }

            if (streamingPortsByUser.isEmpty()) {
                streamingPortsByUser.put(user, initialStreamingPort);
                ports.add(initialStreamingPort);
                return;
            }

            long nextKey = ports.last() + 1;
            streamingPortsByUser.put(user, nextKey);
            ports.add(nextKey);
        }
    }

    private void detachStreamingPort(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkRegistered(user);

            if (!isLoggedIn(user)) {
                throw new UserNotLoggedInException("User not logged in");
            }

            ports.remove(streamingPortsByUser.get(user));
            streamingPortsByUser.remove(user);
        }
    }


    @Override
    public boolean isLoggedIn(User user) {
        return streamingPortsByUser.containsKey(user);
    }


    private void checkRegistered(User user) throws UserNotRegisteredException {
        if (database.doesUserExist(user)) {
            return;
        }

        throw new UserNotRegisteredException(
            "A User with Email: " + user.email() + " and Password: " + user.password() + " does not exist");
    }

    @Override
    public long getPort(User user) {
        if (streamingPortsByUser.containsKey(user)) {
            return streamingPortsByUser.get(user);
        }

        return -1;
    }

    @Override
    public void lockPort(long port) {
        if (!ports.contains(port)) {
            return;
        }

        currentlyStreamingPorts.add(port);
    }

    @Override
    public void isPortLocked(long port) throws PortCurrentlyStreamingException {
        if (currentlyStreamingPorts.contains(port)) {
            throw new PortCurrentlyStreamingException(
                "You are currently listening to a Song. Stop the current one and than try again");
        }
    }

    @Override
    public void freePort(long port) {
        try {
            isPortLocked(port);
        } catch (PortCurrentlyStreamingException ignore) {
            // Ignore - this is what we want
        }

        currentlyStreamingPorts.remove(port);
    }

    public Database getDatabase() {
        return database;
    }

    public Map<User, Long> getStreamingPortsByUser() {
        return streamingPortsByUser;
    }

    public Set<Long> getCurrentlyStreamingPorts() {
        return currentlyStreamingPorts;
    }

    public TreeSet<Long> getPorts() {
        return ports;
    }
}
