package spotify.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import spotify.database.Database;
import spotify.user.User;
import spotify.user.exceptions.UserAlreadyLoggedInException;
import spotify.user.exceptions.UserNotLoggedInException;
import spotify.user.exceptions.UserNotRegisteredException;
import spotify.server.exceptions.PortCurrentlyStreamingException;

import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String TEST_STRING = "testString";

    private Database database;
    private DefaultUserService userService;

    private Map<User, Long> streamingPortsByUser;
    private Map<User, SelectionKey> selectionKeysByUser;
    private Set<Long> currentlyStreamingPorts;
    private TreeSet<Long> ports;

    private User user = new User(TEST_STRING, TEST_STRING);

    {
        database = Mockito.mock(Database.class);
    }

    @BeforeEach
    void beforeEach() {
        userService = new DefaultUserService(6999, database);

        streamingPortsByUser = userService.getStreamingPortsByUser();
        currentlyStreamingPorts = userService.getCurrentlyStreamingPorts();
        ports = userService.getPorts();
    }

    @Test
    void testLogInWorksCorrectly() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        userService.logIn(user);

        assertTrue(streamingPortsByUser.containsKey(user), "User was not marked as logged in");

        long userPort = userService.getPort(user);

        assertTrue(ports.contains(userPort), "User Streaming Port was not included as last");
    }

    @Test
    void testLogInAssignsStreamingPortsCorrectly() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);
        long userPort1 = userService.getPort(user);

        user = new User("new", "User");
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);
        long userPort2 = userService.getPort(user);

        assertEquals(userPort1 + 1, userPort2, "User Streaming Ports not assigned correctly");
    }

    @Test
    void testLogInThrowsUserAlreadyLoggedInExceptionWhenUserIsAlreadyLoggedIn()
        throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);

        assertThrows(UserAlreadyLoggedInException.class, () -> userService.logIn(user),
            "UserAlreadyLoggedInException expected");
    }

    @Test
    void testLogInThrowsUserNotRegisteredExceptionWhenUserDoesNotExist() {
        Mockito.when(database.doesUserExist(user)).thenReturn(false);

        assertThrows(UserNotRegisteredException.class, () -> userService.logIn(user),
            "UserNotRegisteredException expected");
    }

    @Test
    void testIsLoggedInWorksCorrectly() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        userService.logIn(user);

        assertTrue(userService.isLoggedIn(user), "User should be logged in");

        user = new User("not", "logged in");

        assertFalse(userService.isLoggedIn(user), "User should not be logged in");
    }

    @Test
    void testLogOutWorksCorrectly()
        throws UserAlreadyLoggedInException, UserNotRegisteredException, UserNotLoggedInException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);

        long userPort = userService.getPort(user);

        userService.logOut(user);

        assertFalse(streamingPortsByUser.containsKey(user), "User was not marked as logged out");
        assertFalse(ports.contains(userPort), "User was not marked as logged out");
    }

    @Test
    void testLogOutThrowsUserNotLoggedInException() {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        assertThrows(UserNotLoggedInException.class, () -> userService.logOut(user),
            "UserNotLoggedInException expected");
    }

    @Test
    void testLogOutThrowsUserNotRegisteredException() {
        Mockito.when(database.doesUserExist(user)).thenReturn(false);

        assertThrows(UserNotRegisteredException.class, () -> userService.logOut(user),
            "UserNotRegisteredException expected");
    }

    @Test
    void testLockPortMarksUserStreamingPortAsStreaming()
        throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);
        long userPort = userService.getPort(user);

        userService.lockPort(userPort);

        assertTrue(currentlyStreamingPorts.contains(userPort), "User Port not marked as streaming");
    }

    @Test
    void testLockPortDoesNotAddPortWhichHasNotBeenAssignToAUser() {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);

        userService.lockPort(123);

        assertFalse(currentlyStreamingPorts.contains((long) 123), "User Port not marked as streaming");
    }

    @Test
    void testFreePortRemovesPort() throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);
        long userPort = userService.getPort(user);

        userService.lockPort(userPort);

        userService.freePort(userPort);

        assertFalse(currentlyStreamingPorts.contains(userPort), "User Port not marked as not streaming");
    }

    @Test
    void testIsPortLockedThrowsPortCurrentlyStreamingExceptionWhenPortIsStreaming()
        throws UserAlreadyLoggedInException, UserNotRegisteredException {
        Mockito.when(database.doesUserExist(user)).thenReturn(true);
        userService.logIn(user);
        long userPort = userService.getPort(user);

        userService.lockPort(userPort);

        assertThrows(PortCurrentlyStreamingException.class, () -> userService.isPortLocked(userPort),
            "PortCurrentlyStreamingException expected");
    }


}
