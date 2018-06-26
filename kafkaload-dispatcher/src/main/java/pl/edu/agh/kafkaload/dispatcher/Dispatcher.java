package pl.edu.agh.kafkaload.dispatcher;

import pl.edu.agh.kafkaload.consumer.Consumer;
import pl.edu.agh.kafkaload.consumer.ConsumerProperties;
import pl.edu.agh.kafkaload.kafkametrics.KafkaMetrics;
import pl.edu.agh.kafkaload.producer.Producer;
import pl.edu.agh.kafkaload.producer.ProducerProperties;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class Dispatcher {
    private static final AtomicLong sent = new AtomicLong(0);
    private static final AtomicLong received = new AtomicLong(0);

    public static void main(String[] args) {
        String servers = "localhost:9092";

        new Thread(new Producer(
                new ProducerProperties(servers),
                Collections.emptyList()
        )).start();

        new Thread(new Consumer(
                new ConsumerProperties(servers),
                Collections.singleton((value, timestamp) -> System.out.println("Received at " + timestamp))
        )).start();

        new Thread(new KafkaMetrics()).start();
    }
}
