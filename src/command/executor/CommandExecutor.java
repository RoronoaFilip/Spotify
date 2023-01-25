package command.executor;

import command.Command;

public class CommandExecutor {

    public String execute(Command cmd) throws Exception {
        if (cmd == null) {
            return "Invalid Command";
        }

        return cmd.call();
    }
}
