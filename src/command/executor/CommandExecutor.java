package command.executor;


import command.Command;
import storage.Storage;

public class CommandExecutor {

    public String execute(Command cmd) {
        if (cmd == null) {
            return "test";
        }

        String message;
        try {
            message = cmd.call();
        } catch (Exception e) {
            message = e.getMessage();
        }

        return message;
    }
}
