package database;

import playlist.Playlist;
import playlist.exceptions.NoSuchPlaylistException;
import playlist.exceptions.PlaylistAlreadyExistsException;
import song.Song;
import song.exceptions.SongNotFoundException;
import user.User;
import user.exceptions.UserAlreadyExistsException;
import user.exceptions.UserNotRegisteredException;

import java.io.Closeable;
import java.util.Collection;

public interface Database extends AutoCloseable, Closeable {
    String SONGS_FOLDER_DEFAULT = "songs/";
    String DATABASE_FOLDER_DEFAULT = "database/";
    String USERS_FILE_NAME_DEFAULT = "users.txt";
    String PLAYLISTS_FILE_NAME_DEFAULT = "playlists.txt";

    void registerUser(String username, String password) throws UserAlreadyExistsException;

    void addSong(Song song);

    Song getSongBy(String fullName) throws SongNotFoundException;

    Playlist createPlaylist(String playlistName, User owner)
        throws UserNotRegisteredException, PlaylistAlreadyExistsException;

    Playlist getPlaylist(String playlistName, User owner) throws NoSuchPlaylistException;

    Playlist getPlaylistByName(String playlistName) throws NoSuchPlaylistException;

    boolean doesPlaylistExist(Playlist playlist);

    Collection<Song> getMostStreamedSongs();

    Collection<Song> getMostStreamedSongs(int limit);

    Collection<Song> filterSongsBasedOn(String... filter);

    Collection<Song> getAllSongs();

    boolean doesSongExist(Song song);

    boolean doesUserExist(User user);

    String getSongsFolder();
}
