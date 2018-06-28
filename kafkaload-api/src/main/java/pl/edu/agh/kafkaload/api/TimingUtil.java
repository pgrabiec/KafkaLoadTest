package pl.edu.agh.kafkaload.api;

public class TimingUtil {
    private TimingUtil() {
    }

    public static long getNanoTimestamp() {
        return System.nanoTime();
    }

    public static long nanoSince(long timestammp) {
        return getNanoTimestamp() - timestammp;
    }
}
