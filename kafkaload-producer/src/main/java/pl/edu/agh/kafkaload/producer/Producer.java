package pl.edu.agh.kafkaload.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import pl.edu.agh.kafkaload.api.Constants;
import pl.edu.agh.kafkaload.producer.throttle.LoadThrottle;

import java.util.Properties;

public class Producer implements Runnable {
    private final ProducerProperties properties;
    private final String message;
    private final LoadThrottle throttle;
    private final String topic;

    public Producer(ProducerProperties properties, String message, LoadThrottle throttle) {
        this.properties = properties;
        this.message = message;
        this.throttle = throttle;
        this.topic = Constants.TOPIC_NAME;
    }

    @Override
    public void run() {
        System.out.println("Producer started");
        Properties props = properties.getProperties();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        try {
            throttle.start();
            while (!throttle.finished()) {
                throttle.awaitNextSend();
                producer.send(new ProducerRecord<>(topic, message));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Producer finished");
    }
}
