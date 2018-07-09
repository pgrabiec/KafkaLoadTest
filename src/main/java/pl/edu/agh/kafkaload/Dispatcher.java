package pl.edu.agh.kafkaload;

import pl.edu.agh.kafkaload.configuration.ConfigurationChange;
import pl.edu.agh.kafkaload.configuration.ConfigurationReader;
import pl.edu.agh.kafkaload.configuration.TestConfiguration;
import pl.edu.agh.kafkaload.consumer.Consumer;
import pl.edu.agh.kafkaload.consumer.ConsumerProperties;
import pl.edu.agh.kafkaload.kafkametrics.KafkaMetrics;
import pl.edu.agh.kafkaload.kafkametrics.listeners.ChartMetricsListener;
import pl.edu.agh.kafkaload.kafkametrics.listeners.ConsolePrintMetricsListener;
import pl.edu.agh.kafkaload.kafkametrics.listeners.CsvSaveMetricsListener;
import pl.edu.agh.kafkaload.kafkametrics.providers.Unit;
import pl.edu.agh.kafkaload.producer.Producer;
import pl.edu.agh.kafkaload.producer.ProducerProperties;
import pl.edu.agh.kafkaload.util.TimingUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Dispatcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurationReader configurationReader = new ConfigurationReader();
        TestConfiguration conf = configurationReader.readConfiguration("kafkaload.conf");
        final String message = new String(new byte[conf.getMessageSize()]);
        final String outputFile = conf.getOutputFilePattern()
                + "_"
                + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date())
                + ".csv";

        // Sort load changes chronologically
        List<ConfigurationChange> changes = new ArrayList<>(conf.getChanges().size());
        changes.addAll(conf.getChanges());
        changes.sort(Comparator.comparingDouble(ConfigurationChange::getSecond));

        // Prepare executor for client workers
        ExecutorService executor = Executors.newCachedThreadPool();

        // Start the metrics gathering thread right away
        KafkaMetrics kafkaMetrics = new KafkaMetrics(
                Arrays.asList(
                        new ConsolePrintMetricsListener(),
                        new CsvSaveMetricsListener(outputFile, " "),
                        new ChartMetricsListener()
                ),
                conf.getResolution()
        );

        long startTime = TimingUtil.getMillis();
        executor.submit(kafkaMetrics);

        Supplier<Runnable> producerSupplier = () -> new Producer(
                new ProducerProperties(conf.getKafkaServers()),
                message
        );
        Supplier<Runnable> consumerSupplier = () -> new Consumer(
                new ConsumerProperties(conf.getKafkaServers()),
                1000
        );

        ThreadsManager producers = new ThreadsManager(executor);
        ThreadsManager consumers = new ThreadsManager(executor);

        Timer timer = new Timer();
        long lastTime = Long.MIN_VALUE;
        for (ConfigurationChange change : changes) {
            long millisOffset = Math.round(change.getSecond() * Unit.getScaleFactor(Unit.SECOND, Unit.MILLISECOND));
            if (millisOffset >= 0) {
                long time = startTime + millisOffset;
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                adjustWorkers(producerSupplier, consumerSupplier, producers, consumers, change);
                            }
                        },
                        new Date(time)
                );
                if (time > lastTime) {
                    lastTime = time;
                }
            }
        }
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        executor.shutdownNow();
                    }
                },
                new Date(lastTime + 500)
        );
    }

    private static void adjustWorkers(
            Supplier<Runnable> producerSupplier,
            Supplier<Runnable> consumerSupplier,
            ThreadsManager producers,
            ThreadsManager consumers,
            ConfigurationChange change
    ) {
        int newCount = change.getValue();

        switch (change.getType()) {
            case PRODUCER_COUNT:
                System.out.println("Setting producers to " + newCount);
                producers.setWorkers(newCount, producerSupplier);
                break;
            case CONSUMER_COUNT:
                System.out.println("Setting consumers to " + newCount);
                consumers.setWorkers(newCount, consumerSupplier);
                break;
            default:
                System.out.println("Unknown load change type");
        }
    }
}
