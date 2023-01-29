package spotify.server.exceptions;

public class PortCurrentlyStreamingException extends Exception {
    public PortCurrentlyStreamingException(String message) {
        super(message);
    }
}
