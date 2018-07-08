package pl.edu.agh.kafkaload.kafkametrics;

import java.util.List;

public interface MetricsListener {
    void metricsInit(List<String> names);

    void metricsUpdate(List<Double> values);
}
