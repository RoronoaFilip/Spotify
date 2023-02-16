package spotify.user.exceptions;

public class InvalidEmailException extends Exception {
    public InvalidEmailException(String message) {
        super(message);
    }
}
