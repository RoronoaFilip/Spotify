package spotify.database.user.service;

import spotify.database.Database;
import spotify.database.user.User;
import spotify.database.user.exceptions.UserAlreadyLoggedInException;
import spotify.server.exceptions.PortCurrentlyStreamingException;
import spotify.database.user.exceptions.UserNotLoggedInException;
import spotify.database.user.exceptions.UserNotRegisteredException;

import java.util.*;

public class DefaultUserService implements UserService {
    private final long initialStreamingPort;
    private final Database database;

    private final Map<User, Long> streamingPortsByUser;
    private final Set<Long> currentlyStreamingPorts;
    private final PriorityQueue<Long> ports;
    private final Object currentSessionLock = new Object();

    public DefaultUserService(long initialStreamingPort, Database database) {
        this.initialStreamingPort = initialStreamingPort;
        this.database = database;

        streamingPortsByUser = new HashMap<>();
        currentlyStreamingPorts = new HashSet<>();

        ports = new PriorityQueue<>();
        ports.add(initialStreamingPort);
    }

    @Override
    public void logIn(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        if (user == null) {
            return;
        }

        attachStreamingPort(user);
    }

    @Override
    public void logOut(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        if (user == null) {
            return;
        }

        detachStreamingPort(user);
    }

    /**
     * Attaches a streaming port to the {@code user} by picking the first one in the Priority Queue {@code ports}
     * If the Priority Queue is empty after getting the top one, adds the next one in.
     *
     * @param user
     * @throws UserAlreadyLoggedInException if the {@code user} is already logged in
     * @throws UserNotRegisteredException if the {@code user} is not registered
     */
    private void attachStreamingPort(User user) throws UserAlreadyLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkRegistered(user);

            if (isLoggedIn(user)) {
                throw new UserAlreadyLoggedInException("User already logged in");
            }

            long key = ports.poll();
            if (ports.isEmpty()) {
                ports.add(key + 1);
            }

            streamingPortsByUser.put(user, key);
        }
    }

    /**
     * Detaches a streaming port to the {@code user} by removing it from the Map {@code streamingPortsByUser} and adding it to the Priority Queue {@code ports}
     *
     * @param user - the User that is logging out
     * @throws UserNotLoggedInException if the {@code user} is not logged in
     * @throws UserNotRegisteredException if the {@code user} is not registered
     */
    private void detachStreamingPort(User user) throws UserNotLoggedInException, UserNotRegisteredException {
        synchronized (currentSessionLock) {
            checkRegistered(user);

            if (!isLoggedIn(user)) {
                throw new UserNotLoggedInException("User not logged in");
            }

            ports.add(streamingPortsByUser.get(user));
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

    public Queue<Long> getPorts() {
        return ports;
    }
}
