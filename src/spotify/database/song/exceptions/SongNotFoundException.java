package spotify.database.song.exceptions;

public class SongNotFoundException extends Exception {
    public SongNotFoundException(String message) {
        super(message);
    }
}
