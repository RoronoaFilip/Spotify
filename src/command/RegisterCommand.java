package command;

import exceptions.UserAlreadyExistsException;
import storage.InMemoryStorage;
import storage.Storage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterCommand extends Command {
    private String username;
    private String password;
    private Storage storage;

    public RegisterCommand(String username, String password, Storage storage) {
        this.username = username;
        this.password = password;
        this.storage = storage;
    }

    @Override
    public String call() {
        String message;
        try {
            storage.registerUser(username, password);
            message = "User registered successfully";
        } catch (UserAlreadyExistsException e) {
            message = e.getMessage();
        }

        return message;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Storage storage1 = new InMemoryStorage();
        ExecutorService executor = Executors.newCachedThreadPool();
        Set<Future<String>> set = new HashSet<>();

        for (int i = 0; i < 100; ++i) {
            set.add(executor.submit(new RegisterCommand("filip" + i, "123", storage1)));
        }

        for (Future<String> future : set) {
            System.out.println(future.get());
        }

        InMemoryStorage memoryStorage = (InMemoryStorage) storage1;
        System.out.println(memoryStorage.getUsers().size());
        executor.shutdown();
    }
}
