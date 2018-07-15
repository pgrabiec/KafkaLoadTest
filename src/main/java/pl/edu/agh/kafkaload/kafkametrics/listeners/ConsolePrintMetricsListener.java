package pl.edu.agh.kafkaload.kafkametrics.listeners;

import pl.edu.agh.kafkaload.kafkametrics.MetricsListener;

import java.util.List;

public class ConsolePrintMetricsListener implements MetricsListener {
    @Override
    public void metricsInit(List<String> names) {
        System.out.println(names);
    }

    @Override
    public void metricsUpdate(List<Double> values) {
        System.out.println(values);
    }
}
