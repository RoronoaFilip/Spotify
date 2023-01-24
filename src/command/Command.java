package command;

import server.SpotifyServer;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class Command implements Callable<String> {
    protected static final String SUCCESSFUL_LOGIN = "You have logged in successfully";
    protected static final String UNSUCCESSFUL_LOGIN = "You have logged in already";
    protected static final String SUCCESSFUL_LOGOUT = "You have logged out";
    protected static final String UNSUCCESSFUL_LOGOUT = "You have not logged in, you can not log out";
    protected static final String SUCCESSFUL_REGISTER = "You have registered successfully";
    protected static final String UNSUCCESSFUL_REGISTER = "You have registered already";
    protected static final String USER_DOES_NOT_EXIST = "Such User does not exist";

    protected SpotifyServer spotifyServer;

    protected Command(SpotifyServer spotifyServer) {
        this.spotifyServer = spotifyServer;
    }

    protected static String constructMessage(List<?> objects) {
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < objects.size(); ++i) {
            message.append(i + 1).append(". ").append(objects.get(i)).append(System.lineSeparator());
        }

        return message.toString();
    }
}
