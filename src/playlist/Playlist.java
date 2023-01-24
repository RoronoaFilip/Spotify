package playlist;

import song.Song;
import user.User;

import java.util.Collection;

public interface Playlist {
    void addSong(Song song);

    boolean containsSong(Song song);

    Collection<Song> getSongs();

    String getName();

    User getOwner();
}
