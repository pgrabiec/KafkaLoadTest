package pl.edu.agh.kafkaload.api;

@FunctionalInterface
public interface UpdateListener {
    void update(long value, long timestamp);
}
