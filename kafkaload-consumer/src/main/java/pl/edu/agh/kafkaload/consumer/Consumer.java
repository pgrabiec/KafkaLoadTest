package pl.edu.agh.kafkaload.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import pl.edu.agh.kafkaload.api.Constants;
import pl.edu.agh.kafkaload.api.TimingUtil;
import pl.edu.agh.kafkaload.api.UpdateListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class Consumer implements Runnable {
    private final ConsumerProperties properties;
    private final Collection<UpdateListener> listeners;
    private final String topic;

    public Consumer(ConsumerProperties properties, Collection<UpdateListener> listeners, String topic) {
        this.properties = properties;
        this.listeners = listeners;
        this.topic = topic;
    }

    public Consumer(ConsumerProperties properties, Collection<UpdateListener> listeners) {
        this(properties, listeners, Constants.TOPIC_NAME);
    }

    public void run() {
        Properties props = properties.getProperties();

        long timeout = 0;

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(timeout);
            if (!records.isEmpty()) {
                listeners.forEach(listener -> listener.update(records.count(), TimingUtil.getNanoTimestamp()));
            }
        }
    }
}
