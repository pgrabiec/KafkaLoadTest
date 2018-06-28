package pl.edu.agh.kafkaload.dispatcher;

import pl.edu.agh.kafkaload.consumer.Consumer;
import pl.edu.agh.kafkaload.consumer.ConsumerProperties;
import pl.edu.agh.kafkaload.kafkametrics.KafkaMetrics;
import pl.edu.agh.kafkaload.producer.Producer;
import pl.edu.agh.kafkaload.producer.ProducerProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Dispatcher {
    private static final AtomicBoolean finished = new AtomicBoolean(false);

    public static void main(String[] args) throws IOException {
        String servers = "localhost:9092";

        Thread producer = new Thread(new Producer(
                new ProducerProperties(servers),
                Collections.emptyList()
        ));
        producer.start();

        Thread consumer = new Thread(new Consumer(
                new ConsumerProperties(servers),
                Collections.emptyList()
        ));
        consumer.start();

        new Thread(new KafkaMetrics(new FileWriter("./data.csv"), finished)).start();

        new Thread(() -> {
            try {
                Thread.sleep(6000);
                finished.set(true);
                producer.interrupt();
                consumer.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
