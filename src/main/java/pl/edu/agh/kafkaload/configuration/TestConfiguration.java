package pl.edu.agh.kafkaload.configuration;

import java.util.List;

public class TestConfiguration {
    private int messageSize;
    private String outputFilePattern;
    private List<ConfigurationChange> changes;
    private long resolution;
    private String kafkaServers;

    public TestConfiguration(int messageSize, String outputFinePattern, List<ConfigurationChange> changes, long resolution, String kafkaServers) {
        this.messageSize = messageSize;
        this.outputFilePattern = outputFinePattern;
        this.changes = changes;
        this.resolution = resolution;
        this.kafkaServers = kafkaServers;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public String getOutputFilePattern() {
        return outputFilePattern;
    }

    public void setOutputFilePattern(String outputFinePattern) {
        this.outputFilePattern = outputFinePattern;
    }

    public List<ConfigurationChange> getChanges() {
        return changes;
    }

    public void setChanges(List<ConfigurationChange> changes) {
        this.changes = changes;
    }

    public long getResolution() {
        return resolution;
    }

    public void setResolution(long resolution) {
        this.resolution = resolution;
    }

    public String getKafkaServers() {
        return kafkaServers;
    }

    public void setKafkaServers(String kafkaServers) {
        this.kafkaServers = kafkaServers;
    }
}
