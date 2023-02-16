package spotify.server.command.validator;

import spotify.server.command.Command;
import spotify.server.command.CommandType;
import spotify.server.command.thread.unsafe.LoginCommand;
import spotify.user.User;
import spotify.user.exceptions.UserAlreadyLoggedInException;
import spotify.user.exceptions.UserNotLoggedInException;

import java.nio.channels.SelectionKey;

public class CommandValidator {
    /**
     * Attaches User to his Selection Key
     *
     * @param user the User to be attached
     * @param key  the User's Selection Key
     */
    private static void attach(User user, SelectionKey key) {
        key.attach(user);
    }

    /**
     * Verifies if the Login Command has been executed successfully
     * <p>
     * If the Login Command has been executed successfully the User that the Command
     * is tied to gets attached to the current Selection Key
     * </p>
     * <p>
     * If the Login Command has not been executed successfully nothing happens
     * and the User receives the Message generated by the failed Login Command
     * </p>
     *
     * @param cmd the Command to be checked
     * @param key the User's Selection key
     */
    public static void verifyLogin(Command cmd, SelectionKey key) {
        if (cmd == null || cmd.getType() != CommandType.LOGIN_COMMAND) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) cmd;

        if (!loginCommand.isSuccessful()) {
            return;
        }

        User user = loginCommand.getUser();
        attach(user, key);
    }

    /**
     * Checks if the {@code command} is valid for the User attached to the {@code key}
     *
     * <p>
     * A User that has not logged in can only log in and register in to the System
     * </p>
     * <p>
     * A User that has logged in can do everything except log in or register in to the System
     * </p>
     *
     * @param command the Command to be checked
     * @param key     the Selection Key to which a User is attached
     * @throws UserNotLoggedInException     if the User has not logged in to the System
     * @throws UserAlreadyLoggedInException if the User has already been logged in to the System
     */
    public static void checkCommand(Command command, SelectionKey key)
        throws UserNotLoggedInException, UserAlreadyLoggedInException {
        if (command == null) {
            return;
        }
        boolean isLoggedIn = key.attachment() != null;

        if (!isLoggedIn) {
            if (command.getType() == CommandType.REGISTER_COMMAND || command.getType() == CommandType.LOGIN_COMMAND) {
                return;
            }

            throw new UserNotLoggedInException("You have not logged in");
        }

        if (command.getType() == CommandType.LOGIN_COMMAND || command.getType() == CommandType.REGISTER_COMMAND) {
            throw new UserAlreadyLoggedInException("You have already logged in");
        }
    }
}
