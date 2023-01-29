package spotify.database.user.exceptions;

public class UserNotLoggedInException extends Exception {
    public UserNotLoggedInException(String message) {
        super(message);
    }
}
