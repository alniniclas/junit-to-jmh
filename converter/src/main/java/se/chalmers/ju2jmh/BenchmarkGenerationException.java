package se.chalmers.ju2jmh;

public class BenchmarkGenerationException extends Exception {
    public BenchmarkGenerationException() {
    }

    public BenchmarkGenerationException(String message) {
        super(message);
    }

    public BenchmarkGenerationException(Throwable cause) {
        super(cause);
    }

    public BenchmarkGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
