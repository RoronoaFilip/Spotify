package spotify.user.exceptions;

public class UserAlreadyLoggedInException extends Exception {
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }
}
