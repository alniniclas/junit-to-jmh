package se.chalmers.ju2jmh;

public class InvalidInputClassException extends BenchmarkGenerationException {
    public InvalidInputClassException() {
    }

    public InvalidInputClassException(String message) {
        super(message);
    }

    public InvalidInputClassException(Throwable cause) {
        super(cause);
    }

    public InvalidInputClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
