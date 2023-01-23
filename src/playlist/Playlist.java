package playlist;

import song.Song;
import user.User;

public interface Playlist {
    void addSong(Song song);

    boolean containsSong(Song song);

    String getName();

    User getOwner();
}
