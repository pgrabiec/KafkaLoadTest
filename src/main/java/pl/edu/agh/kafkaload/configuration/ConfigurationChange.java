package pl.edu.agh.kafkaload.configuration;

public class ConfigurationChange {
    private final ConfigurationChangeType type;
    private final int value;
    private final double second;

    public ConfigurationChange(ConfigurationChangeType type, int value, double second) {
        this.type = type;
        this.value = value;
        this.second = second;
    }

    public ConfigurationChangeType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public double getSecond() {
        return second;
    }
}
