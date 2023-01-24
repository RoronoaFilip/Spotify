package server.exceptions;

public class PortCurrentlyStreamingException extends Exception {
    public PortCurrentlyStreamingException(String message) {
        super(message);
    }
}
