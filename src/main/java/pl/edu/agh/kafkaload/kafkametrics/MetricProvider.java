package pl.edu.agh.kafkaload.kafkametrics;

public interface MetricProvider {
    double getCurrentValue() throws Exception;

    default void start() throws Exception {}

    String getName();
}
