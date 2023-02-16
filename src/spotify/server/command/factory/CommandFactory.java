package spotify.server.command.factory;

import spotify.server.command.Command;
import spotify.server.command.thread.safe.PlayCommand;
import spotify.server.command.thread.safe.SearchCommand;
import spotify.server.command.thread.safe.ShowPlaylistCommand;
import spotify.server.command.thread.safe.TerminateCommand;
import spotify.server.command.thread.safe.TopSongsCommand;
import spotify.server.command.thread.unsafe.AddSongToPlaylistCommand;
import spotify.server.command.thread.unsafe.CreatePlaylistCommand;
import spotify.server.command.thread.unsafe.DisconnectCommand;
import spotify.server.command.thread.unsafe.LoginCommand;
import spotify.server.command.thread.unsafe.RegisterCommand;
import spotify.database.user.User;
import spotify.server.SpotifyServerTerminatePermission;

/**
 * Creates Command from User Input
 */
public class CommandFactory {
    /**
     * Parsed User input and Creates a Command based on the first Word in the Input
     *
     * @param input         the User Input to be parsed
     * @param user          the User whose Input is Parsed
     * @param spotifyServer the Server
     * @return a parsed Command if the Input is a Valid Command, null otherwise
     */
    public static Command create(String input, User user, SpotifyServerTerminatePermission spotifyServer) {
        if (input == null || input.isBlank()) {
            return null;
        }

        if (input.equalsIgnoreCase(TerminateCommand.COMMAND)) {
            return new TerminateCommand(spotifyServer);
        }

        if (input.equalsIgnoreCase(DisconnectCommand.COMMAND)) {
            return new DisconnectCommand(user, spotifyServer);
        }

        String[] commandSplit = split(input, Command.COMMAND_SPLIT_REGEX, 2);

        if (commandSplit.length != 2) {
            return null;
        }

        String command = commandSplit[0].toLowerCase();

        String commandContent = commandSplit[1];

        return switch (command) {
            case PlayCommand.COMMAND -> PlayCommand.of(commandContent, user, spotifyServer);
            case SearchCommand.COMMAND -> SearchCommand.of(commandContent, spotifyServer);
            case ShowPlaylistCommand.COMMAND -> ShowPlaylistCommand.of(commandContent, spotifyServer);
            case TopSongsCommand.COMMAND -> TopSongsCommand.of(commandContent, spotifyServer);
            case AddSongToPlaylistCommand.COMMAND -> AddSongToPlaylistCommand.of(commandContent, user, spotifyServer);
            case CreatePlaylistCommand.COMMAND -> CreatePlaylistCommand.of(commandContent, user, spotifyServer);
            case LoginCommand.COMMAND -> LoginCommand.of(commandContent, spotifyServer);
            case RegisterCommand.COMMAND -> RegisterCommand.of(commandContent, spotifyServer);
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
