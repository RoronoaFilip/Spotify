package database;

import database.playlist.Playlist;
import database.playlist.PlaylistBase;
import database.playlist.exceptions.NoSuchPlaylistException;
import database.playlist.exceptions.PlaylistAlreadyExistsException;
import database.song.Song;
import database.song.exceptions.SongNotFoundException;
import database.user.User;
import database.user.exceptions.UserAlreadyExistsException;
import database.user.exceptions.UserNotRegisteredException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryDatabase implements Database {
    private String songsFolder = "songs/";
    private String databaseFolder = "database/";
    private String usersFileName = "users.txt";
    private String playlistsFileName = "playlistsByUser.txt";

    private static final String SPACE_REGEX = "\\s+";
    private static final String UNDERLINE_REGEX = "_";

    private Set<User> users;
    private final Object usersRegisterLock = new Object();
    private final Set<Song> songs;
    private Map<User, Set<Playlist>> playlistsByUser;
    private final Object playlistLock = new Object();

    public InMemoryDatabase(String songsFolder, String databaseFolder, String usersFileName, String playlistsFileName) {
        this.songsFolder = songsFolder;
        this.databaseFolder = databaseFolder;
        this.usersFileName = usersFileName;
        this.playlistsFileName = playlistsFileName;

        users = new HashSet<>();
        songs = new HashSet<>();
        playlistsByUser = new HashMap<>();

        readSongsFromFolder();
        readUsersFromFile();
        readPlaylistsFromFile();
    }

    @Override
    public void registerUser(String email, String password) throws UserAlreadyExistsException {
        User toRegister = new User(email, password);

        synchronized (usersRegisterLock) {
            try {
                checkUserUsername(toRegister);
            } catch (UserNotRegisteredException e) {
                users.add(toRegister);
            }
        }
    }

    @Override
    public void addSong(Song song) {
        if (doesSongExist(song)) {
            return;
        }

        songs.add(song);
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

        if (doesPlaylistExist(playlist)) {
            throw new PlaylistAlreadyExistsException(
                "The User: " + owner.email() + " already has a Playlist with the Name: " + playlistName);
        }

        synchronized (playlistLock) {
            playlistsByUser.putIfAbsent(owner, new HashSet<>());
            playlistsByUser.get(owner).add(playlist);
        }

        return playlist;
    }

    @Override
    public Playlist getPlaylist(String playlistName, User owner) throws NoSuchPlaylistException {
        Playlist toFind = new PlaylistBase(playlistName, owner);

        Set<Playlist> userPlaylists = playlistsByUser.get(owner);

        for (Playlist playlist : userPlaylists) {
            if (toFind.equals(playlist)) {
                return playlist;
            }
        }

        throw new NoSuchPlaylistException(
            "User: " + owner.email() + " does not have a Playlist with the Name: " + playlistName);
    }

    @Override
    public Playlist getPlaylistByName(String playlistName) throws NoSuchPlaylistException {
        return playlistsByUser.entrySet().stream().flatMap(entry -> entry.getValue().stream())
            .filter(playlist -> playlist.getName().equalsIgnoreCase(playlistName)).findAny().orElseThrow(
                () -> new NoSuchPlaylistException("A Playlist with the Name: " + playlistName + " does not exist"));
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
    public Collection<Song> getAllSongs() {
        return songs;
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

    @Override
    public boolean doesPlaylistExist(Playlist playlist) {
        return playlistsByUser.entrySet().stream().flatMap(entry -> entry.getValue().stream())
            .anyMatch(toCompare -> toCompare.equals(playlist));
    }

    private void shutdown() {
        writeCollectionToFile(users, databaseFolder, usersFileName);

        Set<Playlist> allPlaylists = playlistsByUser.entrySet().stream().flatMap(entry -> entry.getValue().stream())
            .collect(Collectors.toSet());

        writeCollectionToFile(allPlaylists, databaseFolder, playlistsFileName);
    }

    private void checkUserPassword(User user) throws UserAlreadyExistsException, UserNotRegisteredException {
        for (User check : users) {
            if (user.equals(check)) {
                if (user.password().equals(check.password())) {
                    throw new UserAlreadyExistsException(
                        "A User with Username: " + user.email() + " and Password: " + user.password() +
                        " already exists");
                }
            }
        }

        throw new UserNotRegisteredException(
            "A User with Username: " + user.email() + " and Password: " + user.email() + " does not exists");
    }

    private void checkUserUsername(User user) throws UserAlreadyExistsException, UserNotRegisteredException {
        if (users.contains(user)) {
            throw new UserAlreadyExistsException(
                "A User with Username: " + user.email() + " and Password: " + user.email() + " already exists");
        }

        throw new UserNotRegisteredException(
            "A User with Username: " + user.email() + " and Password: " + user.email() + " does not exists");
    }

    private void readUsersFromFile() {
        String fileName = databaseFolder + usersFileName;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {

            users = reader.lines().map(User::of).collect(Collectors.toSet());

        } catch (IOException ignored) {
            //Database file does not exist yet
        }
    }

    private void readPlaylistsFromFile() {
        String fileName = databaseFolder + playlistsFileName;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {

            playlistsByUser = reader.lines().map(line -> PlaylistBase.of(line, songsFolder))
                .collect(Collectors.groupingBy(Playlist::getOwner, Collectors.toSet()));

        } catch (IOException ignored) {
            //Database file does not exist yet
        }
    }

    private void writeCollectionToFile(Collection<?> objects, String folder, String fileName) {
        String fullFileName = folder + fileName;
        Path databaseFolderPath = Path.of(folder);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(fullFileName))) {

            if (!Files.exists(databaseFolderPath)) {
                Files.createDirectories(databaseFolderPath);
            }

            for (Object object : objects) {
                bufferedWriter.write(object.toString() + System.lineSeparator());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readSongsFromFolder() {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(songsFolder))) {

            for (Path filePath : directoryStream) {
                if (!Files.isDirectory(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    songs.add(Song.of(songsFolder, fileName));
                }
            }

        } catch (IOException e) {
            System.out.println("The Songs Folder could not be opened");
        } catch (SongNotFoundException e) {
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

    @Override
    public String getSongsFolder() {
        return songsFolder;
    }

    public String getDatabaseFolder() {
        return databaseFolder;
    }

    public String getUsersFileName() {
        return usersFileName;
    }

    public String getPlaylistsFileName() {
        return playlistsFileName;
    }
}