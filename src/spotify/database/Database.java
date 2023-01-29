package spotify.database;

import spotify.database.playlist.Playlist;
import spotify.database.playlist.exceptions.NoSuchPlaylistException;
import spotify.database.playlist.exceptions.PlaylistAlreadyExistsException;
import spotify.database.song.Song;
import spotify.database.song.exceptions.SongNotFoundException;
import spotify.database.user.User;
import spotify.database.user.exceptions.InvalidEmailException;
import spotify.database.user.exceptions.UserAlreadyExistsException;
import spotify.database.user.exceptions.UserNotRegisteredException;

import java.io.Closeable;
import java.util.Collection;

/**
 * The Servers Database
 * <p>
 * For a Database to be Constructed the following Parameters must be included in the Constructor:<br>
 * songsFolder - the Folder where Songs will be read from<br>
 * databaseFolder - the Folder where the Users and Playlist must be loaded from<br>
 * usersFileName - the Name of the File where the Users are saved<br>
 * playlistsFilename - the Name of the File where the Playlists are saved<br>
 * The Database Interface has default Values that can be Used
 * </p>
 * <p>
 * If any of the File Paths are Invalid or include invalid Data, the Files/Data will be ignored
 * </p>
 * <p>
 * The close() Method saves all current InMemory Data to the Files specified in the Constructor,
 * creating any non existent
 * </p>
 */
public interface Database extends AutoCloseable, Closeable {
    String SONGS_FOLDER_DEFAULT = "songs/";
    String DATABASE_FOLDER_DEFAULT = "spotifyDatabase/";
    String USERS_FILE_NAME_DEFAULT = "users.txt";
    String PLAYLISTS_FILE_NAME_DEFAULT = "playlists.txt";

    /**
     * Registers a new User to the Database
     *
     * @param email    the User's Email
     * @param password the User's Password
     * @throws UserAlreadyExistsException if a User with the same Email has
     *                                    already registered into the Database
     */
    void registerUser(String email, String password) throws UserAlreadyExistsException, InvalidEmailException;

    /**
     * Adds a Song to the Database
     * <p>
     * Note: for the added Song to be Played,
     * its AudioFormat Data must be included in the Instance
     * </p>
     *
     * @param song the Song to be added
     */
    void addSong(Song song);

    /**
     * Searches a Song based on its full Name
     * <p>
     * A Song full Name is: <br>
     * "SingerName"-"SongName"
     * </p>
     * <p>
     * Note: a Dash ("-") must be present between the Singer Name and the Song Name<br>
     * The Dash could have any Amount of trailing Whitespaces
     * </p>
     *
     * @param fullName the Songs Full Name
     * @return the Song
     * @throws SongNotFoundException if the Song with such full Name does not exist in the Database
     */
    Song getSongBy(String fullName) throws SongNotFoundException;

    /**
     * Creates a new empty Playlist
     *
     * @param playlistName the Playlist's Name
     * @param owner        the Playlist's Owner
     * @return a Reference to the created Playlist
     * @throws UserNotRegisteredException     if the User in not registered in the Database
     * @throws PlaylistAlreadyExistsException if the User already has a Playlist with that Name
     */
    Playlist createPlaylist(String playlistName, User owner)
        throws UserNotRegisteredException, PlaylistAlreadyExistsException;

    /**
     * Searches for a Playlist with the specified Name whose Owner matched {@code owner}
     *
     * @param playlistName the Playlist's Name
     * @param owner        the Owner of the Playlist
     * @return the found Playlist
     * @throws NoSuchPlaylistException if the {@code owner}
     *                                 does not have a Playlist with that Name
     */
    Playlist getPlaylist(String playlistName, User owner) throws NoSuchPlaylistException, UserNotRegisteredException;

    /**
     * Searches for a Playlist with the specified Name
     *
     * @param playlistName the Playlist's Name
     * @return the found Playlist
     * @throws NoSuchPlaylistException if the {@code owner}
     *                                 a Playlist with that Name is not present in the Database
     */
    Playlist getPlaylistByName(String playlistName) throws NoSuchPlaylistException;

    /**
     * Returns all Songs in the Database Sorted by their Streams Count
     *
     * @return a Collection of all Songs in the Database Sorted by their Streams Count
     */
    Collection<Song> getMostStreamedSongs();

    /**
     * Returns the top {@code limit} Songs in the Database Sorted by their Streams Count
     *
     * @return a Collection of  the top {@code limit} Songs in the Database Sorted by their Streams Count
     */
    Collection<Song> getMostStreamedSongs(int limit);

    /**
     * Searched for Songs that Match any of the {@code filters}
     *
     * @param filters the Filters based on which the Songs are filtered
     * @return a Collection of all Songs in the Database that match at least 1 filter
     */
    Collection<Song> filterSongsBasedOn(String... filters);

    Collection<Song> getAllSongs();

    boolean doesSongExist(Song song);

    boolean doesPlaylistExist(Playlist playlist);

    /**
     * Checks of a User whose Email and Password match the {@code user}
     * is registered in the Database
     *
     * @param user the User who is searched
     * @return true if a User whose Email and Password match the {@code user}
     * is registered in the Database, false otherwise
     */
    boolean doesUserExist(User user);

    String getSongsFolder();
}
