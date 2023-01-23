package user;

public record User(String username, String password) {
    private static final String REGEX = ",";
    private static final int USERNAME = 0;
    private static final int PASSWORD = 1;

    public static User of(String line) {
        String[] split = split(line);

        return new User(split[USERNAME], split[PASSWORD]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        User user = (User) o;

        if (!username.equals(user.username))
            return false;
        return password.equals(user.password);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return username + REGEX + password;
    }

    private static String[] split(String line) {
        String[] toReturn = line.split(REGEX);

        for (String str : toReturn) {
            str.strip();
        }

        return toReturn;
    }
}
