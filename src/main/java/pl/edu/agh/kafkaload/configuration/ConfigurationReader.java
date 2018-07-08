package pl.edu.agh.kafkaload.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class ConfigurationReader {
    private static final String COLUMN_SEPARATOR = " ";
    private static final String MESSAGE_SIZE_COMMAND = "message_size";
    private static final String OUTPUT_PATTERN_COMMAND = "output_file_pattern";
    private static final String PRODUCERS_COUNT_COMMAND = "producers";
    private static final String CONSUMERS_COUNT_COMMAND = "consumers";
    private static final String RESOLUTION_COMMAND = "resolution_ms";
    private static final String KAFKA_SERVERS_COMMAND = "kafka_servers";

    private void checkColumns(String[] columns, int argsCount) throws IllegalArgumentException {
        if (columns.length < argsCount + 1) {
            throw new IllegalArgumentException("Invalid number of arguments (expected " + (argsCount - 1) + ") in: " + Arrays.toString(columns));
        }
    }

    private void parseMessageSize(TestConfiguration conf, String[] columns) {
        checkColumns(columns, 1);
        conf.setMessageSize(Integer.parseInt(columns[1]));
    }

    private void parseResolution(TestConfiguration conf, String[] columns) {
        checkColumns(columns, 1);
        conf.setResolution(Long.parseLong(columns[1]));
    }

    private void parseOutputPattern(TestConfiguration conf, String[] columns) {
        checkColumns(columns, 1);
        conf.setOutputFilePattern(columns[1]);
    }

    private void parseKafkaServers(TestConfiguration conf, String[] columns) {
        checkColumns(columns, 1);
        conf.setKafkaServers(columns[1]);
    }

    private void parseProducersCount(TestConfiguration conf, String[] columns) {
        checkColumns(columns, 2);
        double time = Double.parseDouble(columns[1]);
        int producersCount = Integer.parseInt(columns[2]);
        conf.getChanges().add(new ConfigurationChange(
                ConfigurationChangeType.PRODUCER_COUNT,
                producersCount,
                time
        ));
    }

    private void parseConsumersCount(TestConfiguration conf, String[] columns) {
        checkColumns(columns, 2);
        double time = Double.parseDouble(columns[1]);
        int consumersCount = Integer.parseInt(columns[2]);
        conf.getChanges().add(new ConfigurationChange(
                ConfigurationChangeType.CONSUMER_COUNT,
                consumersCount,
                time
        ));
    }

    public TestConfiguration readConfiguration(String configFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));

        TestConfiguration conf = new TestConfiguration(
                1024,
                "result.csv",
                new LinkedList<>(),
                500,
                "localhost:9092"
        );

        while (reader.ready()) {
            String line = reader.readLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split(COLUMN_SEPARATOR);

            String command = columns[0];

            try {
                switch (command) {
                    case MESSAGE_SIZE_COMMAND:
                        parseMessageSize(conf, columns);
                        break;
                    case OUTPUT_PATTERN_COMMAND:
                        parseOutputPattern(conf, columns);
                        break;
                    case PRODUCERS_COUNT_COMMAND:
                        parseProducersCount(conf, columns);
                        break;
                    case CONSUMERS_COUNT_COMMAND:
                        parseConsumersCount(conf, columns);
                        break;
                    case RESOLUTION_COMMAND:
                        parseResolution(conf, columns);
                        break;
                    case KAFKA_SERVERS_COMMAND:
                        parseKafkaServers(conf, columns);
                        break;
                    default:
                        System.out.println("Unknown command in line: " + line);
                }
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }

        return conf;
    }
}
