package spotify.server.command.executor;

import spotify.server.command.Command;

/**
 * Executor that executes Commands
 * null is an Invalid Command
 */
public class CommandExecutor {
    private static final String INVALID_COMMAND = "Invalid Command";

    public String execute(Command cmd) throws Exception {
        if (cmd == null) {
            return INVALID_COMMAND;
        }

        return cmd.call();
    }
}
