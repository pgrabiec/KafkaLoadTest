package pl.edu.agh.kafkaload.util;

import pl.edu.agh.kafkaload.kafkametrics.providers.Unit;

public class TimingUtil {
    private TimingUtil() {
    }

    public static long getMillis() {
        return System.currentTimeMillis();
    }

    public static long millisSince(long timestammp) {
        return getMillis() - timestammp;
    }

    public static double secondsSince(long timestamp) {
        return millisSince(timestamp) * Unit.getScaleFactor(Unit.MILLISECOND, Unit.SECOND);
    }
}
