package pl.edu.agh.kafkaload.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import pl.edu.agh.kafkaload.api.Constants;
import pl.edu.agh.kafkaload.api.TimingUtil;
import pl.edu.agh.kafkaload.api.UpdateListener;

import java.util.Collection;
import java.util.Properties;

public class Producer implements Runnable {
    private static final int MESSAGE_SIZE = 16 * 1024;
    private static final String MESSAGE = new String(new byte[MESSAGE_SIZE]);
    private final ProducerProperties properties;
    private final Collection<UpdateListener> listeners;
    private final String topic;

    public Producer(ProducerProperties properties, Collection<UpdateListener> listeners, String topic) {
        this.properties = properties;
        this.listeners = listeners;
        this.topic = topic;
    }

    public Producer(ProducerProperties properties, Collection<UpdateListener> listeners) {
        this(properties, listeners, Constants.TOPIC_NAME);
    }

    @Override
    public void run() {
        Properties props = properties.getProperties();

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        while (true) {
            producer.send(new ProducerRecord<>(topic, MESSAGE));
            listeners.forEach(listener -> listener.update(1L, TimingUtil.getTimestamp()));
        }
    }
}
