package command;

import command.executor.CommandExecutor;
import storage.InMemoryStorage;
import storage.Storage;
import user.User;

import java.io.IOException;

public class CreatePlaylistCommand extends Command {
    private String playlistName;
    private User owner;

    public CreatePlaylistCommand(String playlistName, User owner, Storage storage) {
        super(storage);
        this.playlistName = playlistName;
        this.owner = owner;
    }

    @Override
    public String call() throws Exception {
        storage.createPlaylist(playlistName, owner);

        String message = "Playlist with Name: " + playlistName + " was created";

        return message;
    }

    public static void main(String[] args) throws IOException {
        try (Storage storage1 = new InMemoryStorage()) {
            CommandExecutor executor = new CommandExecutor();
            User user = new User("filip", "123");

            RegisterCommand registerCommand = new RegisterCommand("filip", "123", storage1);
            System.out.println(executor.execute(registerCommand));

            CreatePlaylistCommand command = new CreatePlaylistCommand("filipPlaylist", user, storage1);
            System.out.println(executor.execute(command));

            AddSongToPlaylistCommand addSongToPlaylistCommand =
                new AddSongToPlaylistCommand("Upsurt-Chekai malko", "filipPlaylist", storage1);
            System.out.println(executor.execute(addSongToPlaylistCommand));

            AddSongToPlaylistCommand addSongToPlaylistCommand1 =
                new AddSongToPlaylistCommand("alabala", "filipPlaylist", storage1);
            System.out.println(executor.execute(addSongToPlaylistCommand1));

            AddSongToPlaylistCommand addSongToPlaylistCommand2 =
                new AddSongToPlaylistCommand("Upsurt-Chekai malko", "alabala", storage1);
            System.out.println(executor.execute(addSongToPlaylistCommand2));
        }
    }
}
