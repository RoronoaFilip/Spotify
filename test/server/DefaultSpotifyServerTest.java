package server;

import command.executor.CommandExecutor;
import database.Database;
import database.user.User;
import database.user.exceptions.UserAlreadyLoggedInException;
import database.user.exceptions.UserNotLoggedInException;
import database.user.exceptions.UserNotRegisteredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import server.exceptions.PortCurrentlyStreamingException;

import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultSpotifyServerTest {
    private static final String TEST_STRING = "testString";

    private Database database;
    private CommandExecutor executor;
    private DefaultSpotifyServer spotifyServer;

    private Map<User, Long> streamingPortsByUser;
    private Map<User, SelectionKey> selectionKeysByUser;
    private Set<Long> currentlyStreamingPorts;
    private TreeSet<Long> ports;

    private User user = new User(TEST_STRING, TEST_STRING);

    {
        database = Mockito.mock(Database.class);
        executor = Mockito.mock(CommandExecutor.class);
    }

    @BeforeEach
    void beforeEach() {
        spotifyServer = new DefaultSpotifyServer(6999, executor, database);

        streamingPortsByUser = spotifyServer.getStreamingPortsByUser();
        selectionKeysByUser = spotifyServer.getSelectionKeysByUser();
        currentlyStreamingPorts = spotifyServer.getCurrentlyStreamingPorts();
        ports = spotifyServer.getPorts();
    }

    @Test
    void testLogInWorksCorrectly() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        spotifyServer.logIn(user);

        assertTrue(streamingPortsByUser.containsKey(user), "User was not marked as logged in");

        long userPort = spotifyServer.getPort(user);

        assertTrue(ports.contains(userPort), "User Streaming Port was not included as last");
    }

    @Test
    void testLogInAssignsStreamingPortsCorrectly() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);
        long userPort1 = spotifyServer.getPort(user);

        user = new User("new", "User");
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);
        long userPort2 = spotifyServer.getPort(user);

        assertEquals(userPort1 + 1, userPort2, "User Streaming Ports not assigned correctly");
    }

    @Test
    void testLogInThrowsUserAlreadyLoggedInExceptionWhenUserIsAlreadyLoggedIn()
        throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);

        assertThrows(UserAlreadyLoggedInException.class, () -> spotifyServer.logIn(user),
            "UserAlreadyLoggedInException expected");
    }

    @Test
    void testLogInThrowsUserNotRegisteredExceptionWhenUserDoesNotExist() {
        Mockito.when(database.doesUserExist(user)).thenReturn(false);

        assertThrows(UserNotRegisteredException.class, () -> spotifyServer.logIn(user),
            "UserNotRegisteredException expected");
    }

    @Test
    void testIsLoggedInWorksCorrectly() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        spotifyServer.logIn(user);

        assertTrue(spotifyServer.isLoggedIn(user), "User should be logged in");

        user = new User("not", "logged in");

        assertFalse(spotifyServer.isLoggedIn(user), "User should not be logged in");
    }

    @Test
    void testLogOutWorkCorrectly()
        throws UserAlreadyLoggedInException, UserNotRegisteredException, UserNotLoggedInException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);
        //selectionKeysByUser.put(user, null);

        long userPort = spotifyServer.getPort(user);

        spotifyServer.logOut(user);

        assertFalse(streamingPortsByUser.containsKey(user), "User was not marked as logged out");
        assertFalse(ports.contains(userPort), "User was not marked as logged out");
        //assertFalse(selectionKeysByUser.containsKey(user), "User Selection Key was not marked as logged out");
    }

    @Test
    void testAddPortStreamingMarksUserStreamingPortAsStreaming()
        throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);
        long userPort = spotifyServer.getPort(user);

        spotifyServer.addPortStreaming(userPort);

        assertTrue(currentlyStreamingPorts.contains(userPort), "User Port not marked as streaming");
    }

    @Test
    void testAddPortStreamingDoesNotAddPortWhichHasNotBeenAssignToAUser() {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        spotifyServer.addPortStreaming(123);

        assertFalse(currentlyStreamingPorts.contains((long) 123), "User Port not marked as streaming");
    }

    @Test
    void testRemovePortStreamingRemovesPort() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);
        long userPort = spotifyServer.getPort(user);

        spotifyServer.addPortStreaming(userPort);

        spotifyServer.removePortStreaming(userPort);

        assertFalse(currentlyStreamingPorts.contains(userPort), "User Port not marked as not streaming");
    }

    @Test
    void testIsPortStreamingThrowsPortCurrentlyStreamingExceptionWhenPortIsStreaming()
        throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        spotifyServer.logIn(user);
        long userPort = spotifyServer.getPort(user);

        spotifyServer.addPortStreaming(userPort);

        assertThrows(PortCurrentlyStreamingException.class, () -> spotifyServer.isPortStreaming(userPort),
            "PortCurrentlyStreamingException expected");
    }
}
