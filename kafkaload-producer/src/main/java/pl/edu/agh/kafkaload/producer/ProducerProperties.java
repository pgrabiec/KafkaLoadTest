package pl.edu.agh.kafkaload.producer;

import java.util.Properties;

public class ProducerProperties {
    private final String servers;

    public ProducerProperties(String servers) {
        this.servers = servers;
    }

    public Properties getProperties() {

        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
//        props.put("enable.auto.commit", "true");
        props.put("acks", "-1");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "10000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return props;
    }
}
