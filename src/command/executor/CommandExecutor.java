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
        //        return switch (cmd.command()) {
        //            case ADD -> addToDo(cmd.arguments());
        //            case COMPLETE -> complete(cmd.arguments());
        //            case LIST -> list(cmd.arguments());
        //            default -> "Unknown command";
        //        };
        return null;
    }
}
