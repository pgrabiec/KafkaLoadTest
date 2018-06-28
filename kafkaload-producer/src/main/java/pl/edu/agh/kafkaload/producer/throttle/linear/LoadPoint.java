package pl.edu.agh.kafkaload.producer.throttle.linear;

/**
 * Represents a point in linear load characteristics
 * */
public class LoadPoint {
    private final long x; // Time in milliseconds
    private final double y; // Load in messages per second

    public LoadPoint(long x, double y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
