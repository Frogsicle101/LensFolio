package nz.ac.canterbury.seng302.portfolio;

public class CheckException extends RuntimeException {
    public CheckException(String errorMessage) {
        super(errorMessage);
    }
}
