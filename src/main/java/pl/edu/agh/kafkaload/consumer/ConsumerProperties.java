package pl.edu.agh.kafkaload.consumer;

import java.util.Properties;

public class ConsumerProperties {
    private final String servers;

    public ConsumerProperties(String servers) {
        this.servers = servers;
    }

    public Properties getProperties() {
        String group = "consumergroup1";

        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
        props.put("group.id", group);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "10000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return props;
    }
}
