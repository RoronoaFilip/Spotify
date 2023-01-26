package command.creator;

import command.Command;
import command.CommandType;
import command.thread.safe.PlayCommand;
import command.thread.safe.SearchCommand;
import command.thread.safe.ShowPlaylistCommand;
import command.thread.safe.TopSongsCommand;
import org.junit.jupiter.api.Test;
import server.DefaultSpotifyServer;
import user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandCreatorTest {
    private static final String PLAY_COMMAND = "play bnr-avantim";
    private static final String SEARCH_COMMAND_NOT_ALL = "search van";
    private static final String SEARCH_COMMAND_ALL = "search all";
    private static final String SHOW_PLAYLIST_COMMAND_NO_OWNER = "show-playlist myPlaylist";
    private static final String SHOW_PLAYLIST_COMMAND_WITH_OWNER = "show-playlist myPlaylist filip";
    private static final String TERMINATE_COMMAND = "terminate";
    private static final String TOP_SONGS_COMMAND_NOT_ALL = "top 10";
    private static final String TOP_SONGS_COMMAND_ALL = "top all";

    private static final String ADD_SONG_TO_PLAYLIST_COMMAND = "add-song-to myPlaylist dim4ou - big meech";
    private static final String CREATE_PLAYLIST_COMMAND = "create-playlist myPlaylist";
    private static final String DISCONNECT_COMMAND = "disconnect";
    private static final String LOGIN_COMMAND = "login filip 123";
    private static final String REGISTER_COMMAND = "register filip 123";

    private final DefaultSpotifyServer spotifyServer = new DefaultSpotifyServer(6999, null, null);
    private final User user = new User("filip", "123");

    @Test
    void testCreateRecognizesPlayCommand() {
        Command actual = CommandCreator.create(PLAY_COMMAND, user, spotifyServer);

        assertEquals(CommandType.PLAY_COMMAND, actual.getType(), "Play Command not parsed correctly");

        PlayCommand actualPlayCommand = (PlayCommand) actual;

        assertEquals("bnr-avantim", actualPlayCommand.getFullSongName(), "Full Song Name not parsed correctly");
        assertEquals(user, actualPlayCommand.getUser(), "User not parsed correctly");
    }

    @Test
    void testCreateRecognizesSearchCommandNotAll() {
        Command actual = CommandCreator.create(SEARCH_COMMAND_NOT_ALL, user, spotifyServer);

        assertEquals(CommandType.SEARCH_COMMAND, actual.getType(), "Search Command not parsed correctly");

        SearchCommand actualSearchCommand = (SearchCommand) actual;

        assertFalse(actualSearchCommand.isAll(), "Not All Search Command not recognized");
    }

    @Test
    void testCreateRecognizesSearchCommandAll() {
        Command actual = CommandCreator.create(SEARCH_COMMAND_ALL, user, spotifyServer);

        assertEquals(CommandType.SEARCH_COMMAND, actual.getType(), "Search Command not parsed correctly");

        SearchCommand actualSearchCommand = (SearchCommand) actual;

        assertTrue(actualSearchCommand.isAll(), "All Search Command not recognized");
    }

    @Test
    void testCreateRecognizesShowPlaylistCommandNoOwner() {
        Command actual = CommandCreator.create(SHOW_PLAYLIST_COMMAND_NO_OWNER, user, spotifyServer);

        assertEquals(CommandType.SHOW_PLAYLIST_COMMAND, actual.getType(), "Show Playlist Command not parsed correctly");

        ShowPlaylistCommand actualShowPlaylistCommand = (ShowPlaylistCommand) actual;

        assertFalse(actualShowPlaylistCommand.hasOwner(), "Show Playlist Command with no Owner not recognized");
    }

    @Test
    void testCreateRecognizesShowPlaylistCommandWithOwner() {
        Command actual = CommandCreator.create(SHOW_PLAYLIST_COMMAND_WITH_OWNER, user, spotifyServer);

        assertEquals(CommandType.SHOW_PLAYLIST_COMMAND, actual.getType(), "Show Playlist Command not parsed correctly");

        ShowPlaylistCommand actualShowPlaylistCommand = (ShowPlaylistCommand) actual;

        assertTrue(actualShowPlaylistCommand.hasOwner(), "Show Playlist Command with Owner not recognized");
    }

    @Test
    void testCreateRecognizesTerminateCommand() {
        Command actual = CommandCreator.create(TERMINATE_COMMAND, user, spotifyServer);

        assertEquals(CommandType.TERMINATE_COMMAND, actual.getType(), "Terminate Command not parsed correctly");
    }

    @Test
    void testCreateRecognizesTopSongsCommandNotAll() {
        Command actual = CommandCreator.create(TOP_SONGS_COMMAND_NOT_ALL, user, spotifyServer);

        assertEquals(CommandType.TOP_SONGS_COMMAND, actual.getType(), "Search Command not parsed correctly");

        TopSongsCommand actualTopSongsCommand = (TopSongsCommand) actual;

        assertFalse(actualTopSongsCommand.isAll(), "Not All Search Command not recognized");
    }

    @Test
    void testCreateRecognizesTopSongsCommandAll() {
        Command actual = CommandCreator.create(TOP_SONGS_COMMAND_ALL, user, spotifyServer);

        assertEquals(CommandType.TOP_SONGS_COMMAND, actual.getType(), "Top Songs Command not parsed correctly");

        TopSongsCommand actualTopSongsCommand = (TopSongsCommand) actual;

        assertTrue(actualTopSongsCommand.isAll(), "Top Songs Command not recognized");
    }

    @Test
    void testCreateRecognizesAddSongsToPlaylistCommand() {
        Command actual = CommandCreator.create(ADD_SONG_TO_PLAYLIST_COMMAND, user, spotifyServer);

        assertEquals(CommandType.ADD_SONG_TO_PLAYLIST_COMMAND, actual.getType(),
            "Add Song to Playlist Command not parsed correctly");
    }

    @Test
    void testCreateRecognizesCreatePlaylistCommand() {
        Command actual = CommandCreator.create(CREATE_PLAYLIST_COMMAND, user, spotifyServer);

        assertEquals(CommandType.CREATE_PLAYLIST_COMMAND, actual.getType(),
            "Create Playlist Command not parsed correctly");
    }

    @Test
    void testCreateRecognizesDisconnectCommand() {
        Command actual = CommandCreator.create(DISCONNECT_COMMAND, user, spotifyServer);

        assertEquals(CommandType.DISCONNECT_COMMAND, actual.getType(), "Disconnect Command not parsed correctly");
    }

    @Test
    void testCreateRecognizesLoginCommand() {
        Command actual = CommandCreator.create(LOGIN_COMMAND, user, spotifyServer);

        assertEquals(CommandType.LOGIN_COMMAND, actual.getType(), "Logic Command not parsed correctly");
    }

    @Test
    void testCreateRecognizesRegisterCommand() {
        Command actual = CommandCreator.create(REGISTER_COMMAND, user, spotifyServer);

        assertEquals(CommandType.REGISTER_COMMAND, actual.getType(), "Register Command not parsed correctly");
    }

    @Test
    void testCreateRecognizesUnknownCommand() {
        Command actual1 = CommandCreator.create("", user, spotifyServer);
        Command actual2 = CommandCreator.create(null, user, spotifyServer);
        Command actual3 = CommandCreator.create("add-song-to", user, spotifyServer);
        Command actual4 = CommandCreator.create("unknown command written here", user, spotifyServer);

        assertNull(actual1, "Unknown Command not parsed correctly");
        assertNull(actual2, "Unknown Command not parsed correctly");
        assertNull(actual3, "Unknown Command not parsed correctly");
        assertNull(actual4, "Unknown Command not parsed correctly");
    }
}
