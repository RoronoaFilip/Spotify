package command.creator;

import command.Command;
import command.safe.PlayCommand;
import command.safe.SearchCommand;
import command.safe.ShowPlaylistCommand;
import command.safe.StopCommand;
import command.safe.TopSongsCommand;
import command.unsafe.AddSongToPlaylistCommand;
import command.unsafe.CreatePlaylistCommand;
import command.unsafe.DisconnectCommand;
import command.unsafe.LoginCommand;
import command.unsafe.RegisterCommand;
import server.SpotifyServer;
import user.User;
import user.exceptions.UserAlreadyLoggedInException;
import user.exceptions.UserNotLoggedInException;

import java.util.Arrays;

public class CommandCreator {
    private static final String USER_COMMAND_REGEX = ":";
    private static final String USER_CREDENTIALS_REGEX = ",";
    private static final String COMMAND_CREDENTIALS_REGEX = "\\s+";

    public static Command create(String input, SpotifyServer spotifyServer) throws UserNotLoggedInException {
        String[] userCommandSplit = split(input, USER_COMMAND_REGEX);

        if (userCommandSplit.length != 2) {
            return null;
        }
        Command toReturn;

        User user = extractUserFromCommand(userCommandSplit[0], USER_CREDENTIALS_REGEX);

        // Login, Register or Disconnect
        toReturn = handleCommandOfLengthOne(userCommandSplit[1], user, spotifyServer);
        if (toReturn != null) {
            return toReturn;
        }

        try {
            spotifyServer.isLoggedIn(user);
        } catch (UserAlreadyLoggedInException e) {
            // Ignore we want this
        }

        //Search
        toReturn = handleSearchCommand(userCommandSplit[1], spotifyServer);
        if (toReturn != null) {
            return toReturn;
        }

        //Add Song to Playlist
        toReturn = handleAddSongToPlaylistCommand(userCommandSplit[1], user, spotifyServer);
        if (toReturn != null) {
            return toReturn;
        }

        //Other Commands
        return handleCommandOfLengthTwo(userCommandSplit[1], user, spotifyServer);
    }

    private static Command handleAddSongToPlaylistCommand(String toSplit, User user, SpotifyServer spotifyServer) {
        final int lengthLimit = 3;
        String[] commandSplit = split(toSplit, COMMAND_CREDENTIALS_REGEX, lengthLimit);

        if (commandSplit.length != lengthLimit) {
            return null;
        }

        String command = commandSplit[0];
        String playlistName = commandSplit[1];
        String songName = commandSplit[2];

        if (!command.equals("add-song-to")) {
            return null;
        }

        return new AddSongToPlaylistCommand(songName, playlistName, user, spotifyServer);
    }

    private static Command handleSearchCommand(String toSplit, SpotifyServer spotifyServer) {
        String[] commandSplit = split(toSplit, COMMAND_CREDENTIALS_REGEX);
        if (commandSplit.length < 2) {
            return null;
        }

        String command = commandSplit[0];
        if (!command.equals("search")) {
            return null;
        }

        return new SearchCommand(Arrays.copyOfRange(commandSplit, 1, commandSplit.length), spotifyServer);
    }

    private static Command handleCommandOfLengthTwo(String toSplit, User user, SpotifyServer spotifyServer) {
        Command toReturn = null;

        String[] commandSplit = split(toSplit, COMMAND_CREDENTIALS_REGEX, 2);
        if (commandSplit.length != 2) {
            return null;
        }

        String command = commandSplit[0];
        String credential = commandSplit[1];

        return switch (command) {
            case "top" -> handleTopSongs(credential, spotifyServer);
            case "play" -> handlePlayCommand(credential, user, spotifyServer);
            case "show-playlist" -> handleShowPlaylistCommand(credential, spotifyServer);
            case "create-playlist" -> handleCreatePlaylistCommand(credential, user, spotifyServer);
            default -> null;
        };
    }

    private static Command handlePlayCommand(String credential, User user, SpotifyServer spotifyServer) {
        return new PlayCommand(credential, user, spotifyServer);
    }

    private static Command handleShowPlaylistCommand(String credential, SpotifyServer spotifyServer) {
        return new ShowPlaylistCommand(credential, spotifyServer);
    }

    private static Command handleCreatePlaylistCommand(String credential, User user, SpotifyServer spotifyServer) {
        return new CreatePlaylistCommand(credential, user, spotifyServer);
    }

    private static Command handleTopSongs(String credential, SpotifyServer spotifyServer) {
        if (credential.equalsIgnoreCase("all")) {
            return new TopSongsCommand(true, spotifyServer);
        }
        try {
            int number = Integer.parseInt(credential);
            return new TopSongsCommand(number, spotifyServer);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Command handleCommandOfLengthOne(String command, User user, SpotifyServer spotifyServer) {
        return switch (command) {
            case "login" -> new LoginCommand(user.username(), user.password(), spotifyServer);
            case "register" -> new RegisterCommand(user.username(), user.password(), spotifyServer);
            case "disconnect" -> new DisconnectCommand(user.username(), user.password(), spotifyServer);
            case "stop" -> new StopCommand(spotifyServer);
            default -> null;
        };
    }

    private static User extractUserFromCommand(String user, final String regex) {
        String[] split = split(user, regex);

        return switch (split.length) {
            case 1 -> new User(split[0], "");
            case 2 -> new User(split[0], split[1]);
            default -> null;
        };
    }

    private static String[] split(String str, final String regex) {
        String[] splitStr = str.split(regex);

        for (String string : splitStr) {
            string = string.strip();
        }

        return splitStr;
    }

    private static String[] split(String str, final String regex, int limit) {
        String[] splitStr = str.split(regex, limit);

        for (String string : splitStr) {
            string = string.strip();
        }

        return splitStr;
    }
}
