package database.playlist.exceptions;

public class PlaylistAlreadyExistsException extends Exception {
    public PlaylistAlreadyExistsException(String message) {
        super(message);
    }
}
