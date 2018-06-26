package pl.edu.agh.kafkaload.api;

public class TimingUtil {
    private TimingUtil() {
    }

    public static long getTimestamp() {
        return System.nanoTime();
    }
}
