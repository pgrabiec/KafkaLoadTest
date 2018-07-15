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
import pl.edu.agh.kafkaload.producer.Producer;
import pl.edu.agh.kafkaload.producer.ProducerProperties;
import pl.edu.agh.kafkaload.util.TimingUtil;
import pl.edu.agh.kafkaload.util.Unit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Dispatcher {
    private static final AtomicInteger consumersCount = new AtomicInteger(0);
    private static final AtomicInteger producersCount = new AtomicInteger(0);

    public static void main(String[] args) throws IOException, InterruptedException {

        ConfigurationReader configurationReader = new ConfigurationReader();
        TestConfiguration conf = configurationReader.readConfiguration("kafkaload.conf");

        System.out.println("ARGS: " + Arrays.toString(args));
        String outputFilePattern;
        if (args.length >= 1) {
            outputFilePattern = args[0];
        } else {
            outputFilePattern = conf.getOutputFilePattern()
                    + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        }

        File destinationFile = new File(outputFilePattern);
        File parentDir = destinationFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

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
                        new CsvSaveMetricsListener(outputFilePattern, ";"),
                        new ChartMetricsListener(outputFilePattern)
                ),
                conf.getResolution(),
                consumersCount,
                producersCount
        );

        executor.submit(kafkaMetrics);

        // Prepare for scheduling workers change events
        final String message = new String(new byte[conf.getMessageSize()]);
        Supplier<Runnable> producerSupplier = () -> new Producer(
                new ProducerProperties(conf.getKafkaServers()),
                message
        );
        Supplier<Runnable> consumerSupplier = () -> new Consumer(
                new ConsumerProperties(conf.getKafkaServers()),
                1000
        );

        kafkaMetrics.blockUntilConnected();
        long startTime = TimingUtil.getMillis();

        scheduleWorkers(changes, executor, startTime, producerSupplier, consumerSupplier);
    }

    private static void scheduleWorkers(List<ConfigurationChange> changes, ExecutorService executor, long startTime, Supplier<Runnable> producerSupplier, Supplier<Runnable> consumerSupplier) {
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
                                switch (change.getType()) {
                                    case PRODUCER_COUNT:
                                        producersCount.set(change.getValue());
                                        break;
                                    case CONSUMER_COUNT:
                                        consumersCount.set(change.getValue());
                                        break;
                                }
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
                        try {
                            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
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
