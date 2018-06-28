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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Dispatcher {

    public static void main(String[] args) throws IOException {
        String servers = "localhost:9092";

        List<RateProvider> rateProviders = new LinearRateFactory().createProviders(
                new FileReader("load.csv")
        );
        for (RateProvider rateProvider : rateProviders) {
            System.out.println("Starting producer");
            new Thread(new Producer(
                    new ProducerProperties(servers),
                    new String(new byte[rateProvider.messageSize()]),
                    new DefaultLoadThrottle(rateProvider))
            ).start();
        }

        new Thread(new Consumer(
                new ConsumerProperties(servers),
                Collections.emptyList()
        )).start();

        new Thread(new KafkaMetrics()).start();
    }
}
