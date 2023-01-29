package spotify.database.playlist;

import spotify.database.song.Song;
import spotify.database.user.User;

import java.util.Collection;

/**
 * Represents a User Playlist
 * <p>
 * Each Playlist has a Name, an Owner and a Collection of the Songs in it
 * </p>
 */
public interface Playlist {
    /**
     * Adds a Song to the Playlist
     *
     * @param song the Song to be added
     */
    void addSong(Song song);

    /**
     * Checks is {@code song} is in the Current Playlist
     *
     * @param song the Song to be checked
     * @return true is the Song is in the Playlist, false otherwise
     */
    boolean containsSong(Song song);

    /**
     * Returns all Songs in the Playlist
     *
     * @return a Collection of the Songs in the Playlist
     */
    Collection<Song> getSongs();

    String getName();

    User getOwner();
}
