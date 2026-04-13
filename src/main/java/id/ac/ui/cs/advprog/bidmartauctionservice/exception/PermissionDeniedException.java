package id.ac.ui.cs.advprog.bidmartauctionservice.exception;

public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
