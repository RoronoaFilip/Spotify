package database;

import playlist.Playlist;
import playlist.PlaylistBase;
import playlist.exceptions.NoSuchPlaylistException;
import playlist.exceptions.PlaylistAlreadyExistsException;
import song.Song;
import song.exceptions.SongNotFoundException;
import user.User;
import user.exceptions.UserAlreadyExistsException;
import user.exceptions.UserNotRegisteredException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryDatabase implements Database {
    private static final String SONGS_FOLDER = "songs/";
    private static final String STORAGE_FOLDER = "database/";
    private static final String USERS_FILE_NAME = "users.txt";
    private static final String PLAYLISTS_FILE_NAME = "playlists.txt";

    private static final String SPACE_REGEX = "\\s+";
    private static final String UNDERLINE_REGEX = "_";

    private Set<User> users;
    private final Object usersRegisterLock = new Object();
    private final Set<Song> songs;
    private Set<Playlist> playlists;
    private final Object playlistLock = new Object();

    public InMemoryDatabase() {
        users = new HashSet<>();
        songs = new HashSet<>();
        playlists = new HashSet<>();

        readUsersFromFile();
        readPlaylistsFromFile();
        readSongsFromFolder();
    }

    @Override
    public void registerUser(String username, String password) throws UserAlreadyExistsException {
        User toRegister = new User(username, password);

        synchronized (usersRegisterLock) {
            try {
                checkUserUsername(toRegister);
            } catch (UserNotRegisteredException e) {
                users.add(toRegister);
            }
        }
    }

    @Override
    public Song getSongBy(String fullName) throws SongNotFoundException {
        String[] split = fullName.split(Song.SINGER_NAME_REGEX);

        if (split.length < 2) {
            throw new SongNotFoundException("A Song with the Name: " + fullName + " was not found");
        }

        String songName = split[1];
        String singerName = split[0];
        Song toGet = new Song(songName, singerName);

        for (Song song : songs) {
            if (toGet.equals(song)) {
                return song;
            }
        }

        throw new SongNotFoundException("A Song with the Name: " + songName + " by " + singerName + " was not found");
    }

    @Override
    public Playlist createPlaylist(String playlistName, User owner)
        throws UserNotRegisteredException, PlaylistAlreadyExistsException {
        try {
            checkUserPassword(owner);
        } catch (UserAlreadyExistsException ignored) {
            // Ignore - that is what we want
        }

        Playlist playlist = new PlaylistBase(playlistName, owner);

        synchronized (playlistLock) {
            if (playlists.contains(playlist)) {
                throw new PlaylistAlreadyExistsException(
                    "The User: " + owner.username() + " already has a Playlist with the Name: " + playlistName);
            }

            playlists.add(playlist);
        }

        return playlist;
    }

    @Override
    public Playlist getPlaylist(String playlistName, User owner) throws NoSuchPlaylistException {
        Playlist toFind = new PlaylistBase(playlistName, owner);

        for (Playlist playlist : playlists) {
            if (toFind.equals(playlist)) {
                return playlist;
            }
        }

        throw new NoSuchPlaylistException(
            "User: " + owner.username() + " does not have a Playlist with the Name: " + playlistName);
    }

    @Override
    public Playlist getPlaylistByName(String playlistName) throws NoSuchPlaylistException {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(playlistName)) {
                return playlist;
            }
        }

        throw new NoSuchPlaylistException("A Playlist with the Name: " + playlistName + " does not exist");
    }

    @Override
    public boolean doesPlaylistExist(Playlist playlist) {
        return playlists.contains(playlist);
    }

    @Override
    public Collection<Song> getMostStreamedSongs() {
        return getMostStreamedSongs(songs.size());
    }

    @Override
    public Collection<Song> getMostStreamedSongs(int limit) {
        return songs.stream().sorted(Comparator.comparingInt(Song::getStreams).reversed()).limit(limit).toList();
    }

    @Override
    public Collection<Song> filterSongsBasedOn(String... filters) {
        return songs.stream().filter(song -> song.doFiltersApply(filters)).toList();
    }

    @Override
    public boolean doesSongExist(Song song) {
        return songs.contains(song);
    }

    @Override
    public boolean doesUserExist(User user) {
        try {
            checkUserPassword(user);
        } catch (UserAlreadyExistsException e) {
            return true;
        } catch (UserNotRegisteredException e) {
            return false;
        }

        return false;
    }

    private void shutdown() {
        saveUsersToFile();
        writePlaylistsToFile();
    }

    private void checkUserPassword(User user) throws UserAlreadyExistsException, UserNotRegisteredException {
        for (User check : users) {
            if (user.equals(check)) {
                if (user.password().equals(check.password())) {
                    throw new UserAlreadyExistsException(
                        "A User with Username: " + user.username() + " and Password: " + user.password() +
                        " already exists");
                }
            }
        }

        throw new UserNotRegisteredException(
            "A User with Username: " + user.username() + " and Password: " + user.username() + " does not exists");
    }

    private void checkUserUsername(User user) throws UserAlreadyExistsException, UserNotRegisteredException {
        if (users.contains(user)) {
            throw new UserAlreadyExistsException(
                "A User with Username: " + user.username() + " and Password: " + user.username() + " already exists");
        }

        throw new UserNotRegisteredException(
            "A User with Username: " + user.username() + " and Password: " + user.username() + " does not exists");
    }

    private void readUsersFromFile() {
        String fileName = STORAGE_FOLDER + USERS_FILE_NAME;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {

            users = reader.lines().map(User::of).collect(Collectors.toSet());

        } catch (IOException ignored) {
            //Database file does not exist yet
        }
    }

    private void saveUsersToFile() {
        String fileName = STORAGE_FOLDER + USERS_FILE_NAME;
        Path of = Path.of(STORAGE_FOLDER);
        try {
            if (!Files.exists(of)) {
                Files.createDirectories(of);
            }

            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(fileName))) {

                for (User user : users) {
                    bufferedWriter.write(user.toString() + System.lineSeparator());
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readPlaylistsFromFile() {
        String fileName = STORAGE_FOLDER + PLAYLISTS_FILE_NAME;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {

            playlists = reader.lines().map(PlaylistBase::of).collect(Collectors.toSet());

        } catch (IOException ignored) {
            //Database file does not exist yet
        }
    }

    private void writePlaylistsToFile() {
        String fileName = STORAGE_FOLDER + PLAYLISTS_FILE_NAME;
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(fileName))) {

            for (Playlist playlist : playlists) {
                bufferedWriter.write(playlist.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readSongsFromFolder() {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(SONGS_FOLDER))) {

            for (Path filePath : directoryStream) {
                if (!Files.isDirectory(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    songs.add(Song.of(fileName));
                }
            }

        } catch (IOException | SongNotFoundException e) {
            System.out.println("A SongFile was not Found"); // This should not happen in this method
        }
    }

    public Set<User> getUsers() {
        return users;
    }

    @Override
    public void close() {
        shutdown();
    }
}