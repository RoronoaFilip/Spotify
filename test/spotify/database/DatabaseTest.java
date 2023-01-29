package spotify.database;

import spotify.database.InMemoryDatabase;
import spotify.database.playlist.Playlist;
import spotify.database.playlist.PlaylistBase;
import spotify.database.playlist.exceptions.NoSuchPlaylistException;
import spotify.database.playlist.exceptions.PlaylistAlreadyExistsException;
import spotify.database.song.Song;
import spotify.database.song.exceptions.SongNotFoundException;
import spotify.database.user.User;
import spotify.database.user.exceptions.InvalidEmailException;
import spotify.database.user.exceptions.UserAlreadyExistsException;
import spotify.database.user.exceptions.UserNotRegisteredException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseTest {
    private static InMemoryDatabase database;

    private static final String TEST_STRING = "testString";
    private static final String REGISTERED_EMAIL = "registered@User";
    private static final String REGISTERED_PASSWORD = "registeredUser";

    private static User user;
    private static Playlist userPlaylist;

    private static Song song1;
    private static Song song2;
    private static Song song3;

    static {
        database = new InMemoryDatabase("songsTestFolderToDelete/", "testDatabaseFolder/", "testUsersFile.txt",
            "testPlaylistsFile.txt");
        // Database will have 0 songs

        user = new User(REGISTERED_EMAIL, REGISTERED_PASSWORD);
        song1 = new Song("Song1", "Song1");
        song2 = new Song("Song2", "Song2");
        song3 = new Song("Song3", "Song3");
    }

    @BeforeAll
    static void setUp() throws UserAlreadyExistsException, PlaylistAlreadyExistsException, UserNotRegisteredException,
        InvalidEmailException {
        database.registerUser(REGISTERED_EMAIL, REGISTERED_PASSWORD);
        userPlaylist = database.createPlaylist(REGISTERED_PASSWORD, user);

        database.addSong(song1);
        database.addSong(song2);
        database.addSong(song3);

        song1.stream();
        song1.stream();
        song1.stream();

        song2.stream();
        song2.stream();

        song3.stream();
    }

    static void deleteDirectoryRecursively(Path pathToDelete) throws IOException {
        if (Files.exists(pathToDelete)) {
            File[] allContents = pathToDelete.toFile().listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectoryRecursively(file.toPath());
                }
            }
            Files.delete(pathToDelete);
        }
    }

    @Test
    void testRegisterUserRegisterUserCorrectly() throws UserAlreadyExistsException, InvalidEmailException {
        database.registerUser("a @ New User", "a New User");

        User newUser = new User("a @ New User", "a New User");

        Set<User> registeredUsers = database.getUsers();

        assertTrue(registeredUsers.contains(newUser), "User not registered");
    }

    @Test
    void testRegisterUserThrowsUserAlreadyExistsExceptionForAlreadyRegisteredUser()
        throws UserAlreadyExistsException, InvalidEmailException {
        database.registerUser("Throw @ Exception", "Throw Exception");

        assertThrows(UserAlreadyExistsException.class,
            () -> database.registerUser("Throw @ Exception", "Throw Exception"), "UserAlreadyExistsException expected");
    }

    @Test
    void testRegisterUserThrowsInvalidEmailExceptionForAlreadyRegisteredUser()
        throws InvalidEmailException, InvalidEmailException, UserAlreadyExistsException {
        assertThrows(InvalidEmailException.class,
            () -> database.registerUser("InvalidEmailException Exception", "InvalidEmailException Exception"),
            "InvalidEmailException expected");
    }

    @Test
    void testGetSongByWorksCorrectly() throws SongNotFoundException {
        assertEquals(song1, database.getSongBy("Song1 - Song1"));
    }

    @Test
    void testGetSongByThrowsSongNotFoundExceptionWhenSongFullNameIsIncorrect() {
        assertThrows(SongNotFoundException.class, () -> database.getSongBy(TEST_STRING),
            "SongNotFoundException expected");
    }

    @Test
    void testGetSongByThrowsSongNotFoundExceptionWhenSongDoesNotExist() {
        assertThrows(SongNotFoundException.class, () -> database.getSongBy(TEST_STRING + " - " + TEST_STRING),
            "SongNotFoundException expected");
    }

    @Test
    void testCreatePlaylistWorksCorrectly()
        throws PlaylistAlreadyExistsException, UserNotRegisteredException, UserAlreadyExistsException,
        InvalidEmailException {
        String email = "Works@Correctly";
        String pass = "WorksCorrectly";

        User newUser = new User(email, pass);
        database.registerUser(email, pass);

        Playlist expected = new PlaylistBase("playlist", newUser);

        Playlist actual = database.createPlaylist("playlist", newUser);

        assertEquals(expected, actual, "The Playlist was not created correctly");
    }

    @Test
    void testCreatePlaylistThrowsPlaylistAlreadyExistsException()
        throws PlaylistAlreadyExistsException, UserNotRegisteredException, UserAlreadyExistsException,
        InvalidEmailException {
        String email = "Throws@Exception";
        String pass = "ThrowsException";

        User newUser = new User(email, pass);
        database.registerUser(email, pass);

        Playlist actual = database.createPlaylist("playlist", newUser);

        assertThrows(PlaylistAlreadyExistsException.class, () -> database.createPlaylist("playlist", newUser),
            "PlaylistAlreadyExistsException expected");
    }

    @Test
    void testCreatePlaylistThrowsUserNotRegisteredException() {
        String email = "NotRegistered@Exception";
        String pass = "NotRegisteredException";

        User newUser = new User(email, pass);

        assertThrows(UserNotRegisteredException.class, () -> database.createPlaylist("playlist", newUser),
            "UserNotRegisteredException expected");
    }

    @Test
    void testGetPlaylistWorksCorrectly() throws NoSuchPlaylistException, UserNotRegisteredException {
        assertEquals(userPlaylist, database.getPlaylist(REGISTERED_PASSWORD, user),
            "The Playlist was not returned correctly");
    }

    @Test
    void testGetPlaylistThrowsUserNotRegisteredException() {
        assertThrows(UserNotRegisteredException.class,
            () -> database.getPlaylist("playlistThatDoesNotExist", new User("Not@Registered", "Not registered")),
            "UserNotRegisteredException expected");
    }

    @Test
    void testGetPlaylistThrowsNoSuchPlaylistException() {
        assertThrows(NoSuchPlaylistException.class, () -> database.getPlaylist("playlistThatDoesNotExist", user),
            "NoSuchPlaylistException expected");
    }

    @Test
    void testGetPlaylistByNameWorksCorrectly() throws NoSuchPlaylistException {
        assertEquals(userPlaylist, database.getPlaylistByName(REGISTERED_PASSWORD),
            "The Playlist was not returned correctly");
    }

    @Test
    void testGetPlaylistByNameThrowsNoSuchPlaylistException() {
        assertThrows(NoSuchPlaylistException.class, () -> database.getPlaylistByName("playlistThatDoesNotExist"),
            "NoSuchPlaylistException expected");
    }

    @Test
    void testDoesPlaylistExistWorksCorrectly() {
        assertTrue(database.doesPlaylistExist(userPlaylist));

        User newUser = new User("newUser", "newUser");
        Playlist playlist = new PlaylistBase("A Playlist That Does Not Exist", newUser);

        assertFalse(database.doesPlaylistExist(playlist));
    }

    @Test
    void testGetMostStreamedSongsWorksCorrectly() {
        List<Song> expected = List.of(song1, song2, song3);

        assertIterableEquals(expected, database.getMostStreamedSongs(), "The Returned Songs were not in correct Order");
    }

    @Test
    void testGetMostStreamedSongsWithLimitWorksCorrectly() {
        List<Song> expected1 = List.of(song1, song2);

        assertIterableEquals(expected1, database.getMostStreamedSongs(2),
            "The Returned Songs with Limit 2 were not in correct Order");

        List<Song> expected2 = List.of(song1);

        assertIterableEquals(expected2, database.getMostStreamedSongs(1),
            "The Returned Songs with Limit 2 were not in correct Order");
    }

    @Test
    void testFilterSongsBasedOnFiltersCorrectly() {
        Collection<Song> expected1 = List.of(song1, song2, song3);
        assertTrue(expected1.containsAll(database.filterSongsBasedOn("1", "2", "3")),
            "The Returned Songs were not correct");

        Collection<Song> expected2 = List.of(song2);
        assertTrue(expected2.containsAll(database.filterSongsBasedOn("2")), "The Returned Songs were not correct");

        Collection<Song> expected3 = List.of(song1, song3);
        assertTrue(expected3.containsAll(database.filterSongsBasedOn("1", "3")), "The Returned Songs were not correct");
    }

    @Test
    void testDoesUserExistWorkCorrectly() {
        assertTrue(database.doesUserExist(user), "False was returned when a User exists");
        assertFalse(database.doesUserExist(new User("Unknown", "Unknown")),
            "True was returned when a User does not exists");
    }

    @Test
    void testCloseCreatesFiles() throws IOException {
        database.close();

        InMemoryDatabase database1 = (InMemoryDatabase) database;

        Path usersPath = Path.of(database1.getDatabaseFolder() + database1.getUsersFileName());
        assertTrue(Files.exists(usersPath), "A File for the Users was not Created");

        Path playlistsPath = Path.of(database1.getDatabaseFolder() + database1.getPlaylistsFileName());
        assertTrue(Files.exists(playlistsPath), "A File for the playlists was not Created");

        Path databaseFolder = Path.of(database1.getDatabaseFolder());
        deleteDirectoryRecursively(databaseFolder);
    }
}
