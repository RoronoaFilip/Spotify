package spotify.user;

import spotify.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {
    private User user = new User("filip", "filip");
    private String userString = "filip,filip";

    @Test
    void testUserOfParsesStringCorrectly() {
        User newUser = User.of(userString);

        assertEquals(user, newUser, "User not parsed correctly");
    }
}
