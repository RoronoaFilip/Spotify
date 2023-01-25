package command.creator;

import command.Command;
import command.thread.safe.PlayCommand;
import command.thread.safe.SearchCommand;
import command.thread.safe.ShowPlaylistCommand;
import command.thread.safe.TerminateCommand;
import command.thread.safe.TopSongsCommand;
import command.thread.unsafe.AddSongToPlaylistCommand;
import command.thread.unsafe.CreatePlaylistCommand;
import command.thread.unsafe.DisconnectCommand;
import command.thread.unsafe.LoginCommand;
import command.thread.unsafe.RegisterCommand;
import server.StreamingSpotifyServer;
import user.User;

public class CommandCreator {
    public static Command create(String input, User user, StreamingSpotifyServer spotifyServer) {
        if (input == null || input.isBlank()) {
            return null;
        }

        if (input.equalsIgnoreCase("terminate")) {
            return new TerminateCommand(spotifyServer);
        }

        if (input.equalsIgnoreCase("disconnect")) {
            return new DisconnectCommand(user, spotifyServer);
        }

        String[] commandSplit = split(input, Command.COMMAND_SPLIT_REGEX, 2);

        if (commandSplit.length != 2) {
            return null;
        }

        String command = commandSplit[0].toLowerCase();

        String commandContent = commandSplit[1];

        return switch (command) {
            case "register" -> RegisterCommand.of(commandContent, spotifyServer);
            case "login" -> LoginCommand.of(commandContent, spotifyServer);
            case "search" -> SearchCommand.of(commandContent, spotifyServer);
            case "top" -> TopSongsCommand.of(commandContent, spotifyServer);
            case "create-playlist" -> CreatePlaylistCommand.of(commandContent, user, spotifyServer);
            case "add-song-to" -> AddSongToPlaylistCommand.of(commandContent, user, spotifyServer);
            case "show-playlist" -> ShowPlaylistCommand.of(commandContent, spotifyServer);
            case "play" -> PlayCommand.of(commandContent, user, spotifyServer);
            default -> null;
        };
    }

    private static String[] split(String str, final String regex, int limit) {
        String[] splitStr = str.split(regex, limit);

        for (String string : splitStr) {
            string = string.strip();
        }

        return splitStr;
    }
}
