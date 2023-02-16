package spotify.server.command.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import spotify.server.command.CommandType;
import spotify.server.command.thread.safe.PlayCommand;
import spotify.server.command.thread.unsafe.LoginCommand;
import spotify.database.user.User;
import spotify.database.user.exceptions.UserNotLoggedInException;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CommandValidatorTest {

    private SelectionKey selectionKey = Mockito.mock(SelectionKey.class);

    @Test
    void testVerifyLoginAttachesCorrectly() {
        LoginCommand loginCommand = Mockito.mock(LoginCommand.class);
        Mockito.when(loginCommand.isSuccessful()).thenReturn(true);
        Mockito.when(loginCommand.getType()).thenReturn(CommandType.LOGIN_COMMAND);
        User user = new User("filip", "123");
        Mockito.when(loginCommand.getUser()).thenReturn(user);

        CommandValidator.verifyLogin(loginCommand, selectionKey);

        assertEquals(user, selectionKey.attachment(), "User was not attached");
    }

    @Test
    void testVerifyLoginDoesNotAttachCorrectly() {
        LoginCommand loginCommand = Mockito.mock(LoginCommand.class);
        Mockito.when(loginCommand.isSuccessful()).thenReturn(false);
        Mockito.when(loginCommand.getType()).thenReturn(CommandType.LOGIN_COMMAND);

        CommandValidator.verifyLogin(loginCommand, selectionKey);

        assertNull(selectionKey.attachment(), "User must not be attached");
    }

    @Test
    void testVerifyLoginDoesNotAttachCorrectlyWhenCommandIsNotLogin() {
        LoginCommand loginCommand = Mockito.mock(LoginCommand.class);
        Mockito.when(loginCommand.getType()).thenReturn(CommandType.REGISTER_COMMAND);

        CommandValidator.verifyLogin(loginCommand, selectionKey);

        assertNull(selectionKey.attachment(), "User must not be attached");
    }

    @Test
    void testCheckCommandThrowsUserNotLoggedInException() {
        PlayCommand playCommand = Mockito.mock(PlayCommand.class);
        Mockito.when(playCommand.getType()).thenReturn(CommandType.PLAY_COMMAND);
        SelectionKey key = Mockito.mock(SelectionKey.class);

        assertThrows(UserNotLoggedInException.class, () -> CommandValidator.checkCommand(playCommand, key),
            "UserNotLoggedInException expected");
    }

    @Test
    void testCheckCommandDoesNotThrowUserNotLoggedInException() {
        LoginCommand loginCommand = Mockito.mock(LoginCommand.class);
        Mockito.when(loginCommand.getType()).thenReturn(CommandType.LOGIN_COMMAND);
        SelectionKey key = Mockito.mock(SelectionKey.class);

        assertDoesNotThrow(() -> CommandValidator.checkCommand(loginCommand, key),
            "UserNotLoggedInException not expected");
    }
}
