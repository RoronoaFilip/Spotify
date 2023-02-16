package spotify.server.command;

public enum CommandType {
    PLAY_COMMAND("play"), SEARCH_COMMAND("search"), SHOW_PLAYLIST_COMMAND("show-playlist"),
    TERMINATE_COMMAND("terminate"), TOP_SONGS_COMMAND("top"), ADD_SONG_TO_PLAYLIST_COMMAND("add-song-to"),
    CREATE_PLAYLIST_COMMAND("create-playlist"), DISCONNECT_COMMAND("disconnect"), LOGIN_COMMAND("login"),
    REGISTER_COMMAND("register");

    private final String asString;

    CommandType(String asString) {
        this.asString = asString;
    }

    public String getAsString() {
        return asString;
    }
}
