package spotify.user;

/**
 * Represents a User in the System
 *
 * @param email    the Email of the User
 * @param password the Password of the User
 */
public record User(String email, String password) {
    private static final String REGEX = ",";
    private static final int EMAIL = 0;
    private static final int PASSWORD = 1;

    public static User of(String line) {
        String[] split = split(line);

        return new User(split[EMAIL], split[PASSWORD]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        User user = (User) o;

        if (!email.equalsIgnoreCase(user.email))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        String usernameLowerCase = email.toLowerCase();

        return usernameLowerCase.hashCode();
    }

    @Override
    public String toString() {
        return email + REGEX + password;
    }

    private static String[] split(String line) {
        String[] toReturn = line.split(REGEX);

        for (String str : toReturn) {
            str.strip();
        }

        return toReturn;
    }
}
