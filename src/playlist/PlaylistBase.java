package playlist;

import song.Song;
import song.exceptions.SongNotFoundException;
import user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaylistBase implements Playlist {
    private static final int SPLIT_SIZE_LIMIT = 3;
    private static final String OWNER_SONGS_REGEX = ":";

    private final Set<Song> songs;
    private final String name;
    private final User owner;

    public PlaylistBase(String name, User owner) {
        songs = new HashSet<>();
        this.name = name;
        this.owner = owner;
    }

    PlaylistBase(Set<Song> songs, String name, User owner) {
        this.songs = songs;
        this.name = name;
        this.owner = owner;
    }

    public static Playlist of(String line, Collection<Song> songsInDatabase) {
        String[] ownerSongsSplit = line.split(OWNER_SONGS_REGEX, SPLIT_SIZE_LIMIT);

        User owner = User.of(ownerSongsSplit[0]);
        String playlistName = ownerSongsSplit[1];

        Set<Song> songSet = new HashSet<>();

        String[] songsSplit = ownerSongsSplit[2].split(",");
        for (String songLine : songsSplit) {
            try {
                if (songLine.isBlank()) {
                    continue;
                }
                songSet.add(Song.of(songLine, songsInDatabase));
            } catch (SongNotFoundException e) {
                // Skip song that isn't in database
            }
        }

        return new PlaylistBase(songSet, playlistName, owner);
    }

    @Override
    public synchronized void addSong(Song song) {
        songs.add(song);
    }

    @Override
    public boolean containsSong(Song song) {
        return songs.contains(song);
    }

    @Override
    public Collection<Song> getSongs() {
        return songs;
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

        if (!name.equalsIgnoreCase(that.name))
            return false;
        return owner.equals(that.owner);
    }

    @Override
    public int hashCode() {
        String nameLoweCase = name.toLowerCase();

        int result = nameLoweCase.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return owner.toString() + OWNER_SONGS_REGEX + name + OWNER_SONGS_REGEX +
               songs.stream().map(Song::toString).collect(Collectors.joining(",")) + System.lineSeparator();
    }
}
