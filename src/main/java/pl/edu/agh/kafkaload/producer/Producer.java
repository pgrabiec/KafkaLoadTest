package pl.edu.agh.kafkaload.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import pl.edu.agh.kafkaload.util.Constants;

import java.util.Properties;

public class Producer implements Runnable {
    private final ProducerProperties properties;
    private final String message;
    private final String topic;

    public Producer(ProducerProperties properties, String message) {
        this.properties = properties;
        this.message = message;
        this.topic = Constants.TOPIC_NAME;
    }

    @Override
    public void run() {
        System.out.println("Producer started");
        Properties props = properties.getProperties();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        try {
            while (true) {
                producer.send(new ProducerRecord<>(topic, message));
            }
        } catch (Exception ex) {
            System.out.println("Producer exit: " + ex.getClass().getSimpleName());
        }
    }
}
