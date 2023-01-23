package command;

import command.exceptions.UnsuccessfulRegistrationException;
import storage.Storage;
import user.exceptions.UserAlreadyExistsException;

public class RegisterCommand extends Command {
    private final String username;
    private final String password;

    public RegisterCommand(String username, String password, Storage storage) {
        super(storage);
        this.username = username;
        this.password = password;
    }

    @Override
    public String call() throws Exception {
        String message;
        try {
            storage.registerUser(username, password);
            message = SUCCESSFUL_REGISTER;
        } catch (UserAlreadyExistsException e) {
            throw new UnsuccessfulRegistrationException(UNSUCCESSFUL_REGISTER);
        }

        return message;
    }
}
