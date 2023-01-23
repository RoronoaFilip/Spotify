package playlist;

import song.Song;
import user.User;

import java.util.HashSet;
import java.util.Set;

public class PlaylistBase implements Playlist {
    private final Set<Song> songs;
    private final String name;
    private final User owner;

    public PlaylistBase(String name, User owner) {
        songs = new HashSet<>();
        this.name = name;
        this.owner = owner;
    }

    @Override
    public void addSong(Song song) {
        songs.add(song);
    }

    @Override
    public boolean containsSong(Song song) {
        return songs.contains(song);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PlaylistBase that = (PlaylistBase) o;

        if (!name.equals(that.name))
            return false;
        return owner.equals(that.owner);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }
}
