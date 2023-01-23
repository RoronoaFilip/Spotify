package command;

import command.exceptions.UnsuccessfulRegistrationException;
import user.exceptions.UserAlreadyExistsException;
import server.SpotifyServer;
import storage.InMemoryStorage;
import storage.Storage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterCommand extends Command {
    private final String username;
    private final String password;
    private final Storage storage;

    public RegisterCommand(String username, String password, Storage storage) {
        this.username = username;
        this.password = password;
        this.storage = storage;
    }

    @Override
    public String call() throws Exception {
        String message;
        try {
            storage.registerUser(username, password);
            message = SUCCESSFUL_REGISTER;
        } catch (UserAlreadyExistsException e) {
            throw new UnsuccessfulRegistrationException(UNSUCCESSFUL_REGISTER);
        }

        return message;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SpotifyServer server = new SpotifyServer(7777, null);
        Storage storage1 = server.getStorage();
        ExecutorService executor = Executors.newCachedThreadPool();
        Set<Future<String>> set = new HashSet<>();

        for (int i = 0; i < 100; ++i) {
            set.add(executor.submit(new RegisterCommand("filip", "123", storage1)));
        }

        for (Future<String> future : set) {
            System.out.println(future.get());
        }

        InMemoryStorage memoryStorage = (InMemoryStorage) storage1;
        System.out.println(memoryStorage.getUsers().size());
        executor.shutdown();
    }
}
