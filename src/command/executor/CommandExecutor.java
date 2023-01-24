package command.executor;


import command.Command;

public class CommandExecutor {

    public String execute(Command cmd) {
        if (cmd == null) {
            return "Invalid Command";
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
