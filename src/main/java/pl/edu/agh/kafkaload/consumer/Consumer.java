package pl.edu.agh.kafkaload.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import pl.edu.agh.kafkaload.util.Constants;

import java.util.Collections;
import java.util.Properties;

public class Consumer implements Runnable {
    private final ConsumerProperties properties;
    private final String topic;
    private final long consumerPollTimeout;

    public Consumer(ConsumerProperties properties, long consumerPollTimeout) {
        this.properties = properties;
        this.consumerPollTimeout = consumerPollTimeout;
        this.topic = Constants.TOPIC_NAME;
    }

    public void run() {
        System.out.println("Consumer started");
        Properties props = properties.getProperties();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                consumer.poll(consumerPollTimeout);
            }
        } catch (Exception ex) {
            System.out.println("Consumer finished: " + ex.getClass().getSimpleName());
        }
    }
}
