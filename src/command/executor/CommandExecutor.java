package command.executor;


import command.Command;
import storage.Storage;

public class CommandExecutor {
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
        "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";

    private static final String ADD = "add-todo";
    private static final String COMPLETE = "complete-todo";
    private static final String LIST = "list";

    private Storage storage;

    public CommandExecutor(Storage storage) {
        this.storage = storage;
    }

    public String execute(Command cmd) {
        String message;
        try {
            message = cmd.call();
        } catch (Exception e) {
            message = e.getMessage();
        }

        return message;
    }
}
