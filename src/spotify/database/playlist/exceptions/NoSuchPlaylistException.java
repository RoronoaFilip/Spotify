package spotify.database.playlist.exceptions;

public class NoSuchPlaylistException extends Exception {
    public NoSuchPlaylistException(String message) {
        super(message);
    }
}
