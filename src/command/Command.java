package command;

import song.Song;
import storage.Storage;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class Command implements Callable<String> {
    protected static final String SUCCESSFUL_LOGIN = "You have logged in successfully";
    protected static final String UNSUCCESSFUL_LOGIN = "You have logged in already";
    protected static final String SUCCESSFUL_REGISTER = "You have registered successfully";
    protected static final String UNSUCCESSFUL_REGISTER = "You have registered already";
    protected static final String USER_DOES_NOT_EXIST = "Such User does not exist";

    protected Storage storage;

    protected Command(Storage storage) {
        this.storage = storage;
    }

    protected static String constructMessage(List<Song> songs) {
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < songs.size(); ++i) {
            message.append(i + 1).append(songs.get(i)).append(System.lineSeparator());
        }

        return message.toString();
    }
}
