package pl.edu.agh.kafkaload.kafkametrics.listeners;

import pl.edu.agh.kafkaload.kafkametrics.MetricsListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CsvSaveMetricsListener implements MetricsListener {
    private final Writer writer;
    private final String separator;

    public CsvSaveMetricsListener(String fileNameWithoutExtension, String separator) throws IOException {
        this.writer = new BufferedWriter(
                new FileWriter(fileNameWithoutExtension + ".csv")
        );
        this.separator = separator;
    }

    @Override
    public void metricsInit(List<String> names) {
        try {
            writer.write(String.join(separator, names));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void metricsUpdate(List<Double> values) {
        try {
            writer.write('\n');
            writer.write(String.join(
                    separator,
                    values.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList())
            ));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
