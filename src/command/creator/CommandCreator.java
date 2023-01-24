package command.creator;

import command.Command;
import command.safe.PlayCommand;
import command.safe.SearchCommand;
import command.safe.ShowPlaylistCommand;
import command.safe.TerminateCommand;
import command.safe.TopSongsCommand;
import command.unsafe.AddSongToPlaylistCommand;
import command.unsafe.CreatePlaylistCommand;
import command.unsafe.DisconnectCommand;
import command.unsafe.LoginCommand;
import command.unsafe.RegisterCommand;
import server.SpotifyServer;
import user.User;

import java.nio.channels.SelectionKey;

public class CommandCreator {
    public static Command create(String input, SelectionKey key, SpotifyServer spotifyServer) {
        if (input == null || input.isBlank()) {
            return null;
        }

        if (input.equalsIgnoreCase("terminate")) {
            return new TerminateCommand(spotifyServer);
        }

        if (input.equalsIgnoreCase("disconnect")) {
            return new DisconnectCommand(key, spotifyServer);
        }

        String[] commandSplit = split(input, Command.COMMAND_SPLIT_REGEX, 2);

        if (commandSplit.length != 2) {
            return null;
        }

        String command = commandSplit[0].toLowerCase();

        String commandContent = commandSplit[1];

        User user = null;
        if (key.attachment() != null) {
            user = (User) key.attachment();
        }

        return switch (command) {
            case "register" -> RegisterCommand.of(commandContent, spotifyServer);
            case "login" -> LoginCommand.of(commandContent, key, spotifyServer);
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
