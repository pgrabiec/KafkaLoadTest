package pl.edu.agh.kafkaload.dispatcher;

import pl.edu.agh.kafkaload.consumer.Consumer;
import pl.edu.agh.kafkaload.consumer.ConsumerProperties;
import pl.edu.agh.kafkaload.kafkametrics.KafkaMetrics;
import pl.edu.agh.kafkaload.producer.Producer;
import pl.edu.agh.kafkaload.producer.ProducerProperties;
import pl.edu.agh.kafkaload.producer.throttle.DefaultLoadThrottle;
import pl.edu.agh.kafkaload.producer.throttle.RateProvider;
import pl.edu.agh.kafkaload.producer.throttle.linear.LinearRateFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dispatcher {
    private static final AtomicBoolean finished = new AtomicBoolean(false);

    public static void main(String[] args) throws IOException {
        String servers = "localhost:9092";

        List<Thread> threads = new LinkedList<>();

        List<RateProvider> rateProviders = new LinearRateFactory().createProviders(
                new FileReader("load.csv")
        );
        for (RateProvider rateProvider : rateProviders) {
            System.out.println("Starting producer");
            Thread producer = new Thread(new Producer(
                    new ProducerProperties(servers),
                    new String(new byte[rateProvider.messageSize()]),
                    new DefaultLoadThrottle(rateProvider))
            );
            producer.start();
            threads.add(producer);
        }

        Thread consumer = new Thread(new Consumer(
                new ConsumerProperties(servers),
                Collections.emptyList()
        ));
        consumer.start();
        threads.add(consumer);

        new Thread(new KafkaMetrics(new FileWriter("./data.csv"), finished)).start();

        new Thread(() -> {
            try {
                Thread.sleep(30000);
                finished.set(true);
                threads.forEach(Thread::interrupt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
