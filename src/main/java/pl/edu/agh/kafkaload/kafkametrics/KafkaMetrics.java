package pl.edu.agh.kafkaload.kafkametrics;

import pl.edu.agh.kafkaload.kafkametrics.providers.ErrorRateProvider;
import pl.edu.agh.kafkaload.kafkametrics.providers.MeanReverseCalculateMetricProvider;
import pl.edu.agh.kafkaload.kafkametrics.providers.RateMetricProvider;
import pl.edu.agh.kafkaload.kafkametrics.providers.SingleValueMetricProvider;
import pl.edu.agh.kafkaload.util.TimingUtil;
import pl.edu.agh.kafkaload.util.Unit;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KafkaMetrics implements Runnable {
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Lock connectionLock = new ReentrantLock();
    private final Condition connectedCondition = connectionLock.newCondition();
    private final List<MetricsListener> listeners;
    private final long resolutionMilliseconds;
    private final AtomicInteger consumersCount;
    private final AtomicInteger producersCount;

    public KafkaMetrics(List<MetricsListener> listeners, long resolutionMilliseconds, AtomicInteger consumersCount, AtomicInteger producersCount) {
        this.listeners = listeners;
        this.resolutionMilliseconds = resolutionMilliseconds;
        this.consumersCount = consumersCount;
        this.producersCount = producersCount;
    }

    private void beforeExecution(List<MetricProvider> providers) throws Exception {
        for (MetricProvider provider : providers) {
            provider.start();
        }

        List<String> names = new ArrayList<>(providers.size() + 1);
        names.add("time (s)");
        providers.forEach(provider -> names.add(provider.getName()));

        for (MetricsListener listener : listeners) {
            listener.metricsInit(names);
        }
    }

    private List<MetricProvider> getProviders(MBeanServerConnection connection) throws MalformedObjectNameException {
        return Arrays.asList(

                new RateMetricProvider(
                        "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec",
                        "Count",
                        connection,
                        "input (MB/s)",
                        Unit.BYTE,
                        Unit.MEGABYTE
                ),

                new RateMetricProvider(
                        "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec",
                        "Count",
                        connection,
                        "output (MB/s)",
                        Unit.BYTE,
                        Unit.MEGABYTE
                ),

                new SingleValueMetricProvider(
                        "kafka.network:type=RequestChannel,name=RequestQueueSize",
                        "Value",
                        connection,
                        "request queue size",
                        Unit.UNIT,
                        Unit.UNIT
                ),

                new MeanReverseCalculateMetricProvider(
                        "kafka.network:type=RequestMetrics,name=LocalTimeMs,request=Produce",
                        "Count",
                        "Mean",
                        connection,
                        "processing time (0.1 s)",
                        Unit.MILLISECOND,
                        Unit.DECY_SECOND
                ),

                new MeanReverseCalculateMetricProvider(
                        "kafka.network:type=RequestMetrics,name=RequestQueueTimeMs,request=Produce",
                        "Count",
                        "Mean",
                        connection,
                        "queue wait time (0.1s)",
                        Unit.MILLISECOND,
                        Unit.DECY_SECOND
                ),

                new MeanReverseCalculateMetricProvider(
                        "kafka.log:type=LogFlushStats,name=LogFlushRateAndTimeMs",
                        "Count",
                        "Mean",
                        connection,
                        "log flush time (0.1s)",
                        Unit.MILLISECOND,
                        Unit.DECY_SECOND
                ),

                new RateMetricProvider(
                        "kafka.log:type=LogFlushStats,name=LogFlushRateAndTimeMs",
                        "Count",
                        connection,
                        "log flush rate (1/s)",
                        Unit.UNIT,
                        Unit.UNIT
                ),

                new RateMetricProvider(
                        "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec",
                        "Count",
                        connection,
                        "messages in (0.5k/s)",
                        Unit.UNIT,
                        Unit.HALF_KILO
                ),

                new ErrorRateProvider(
                        "error rate (0.1/s)",
                        connection
                ),

                new MetricProvider() {
                    @Override
                    public double getCurrentValue() {
                        return producersCount.get();
                    }

                    @Override
                    public String getName() {
                        return "producers";
                    }
                },

                new MetricProvider() {
                    @Override
                    public double getCurrentValue() throws Exception {
                        return consumersCount.get();
                    }

                    @Override
                    public String getName() {
                        return "consumers";
                    }
                }
        );
    }

    @Override
    public void run() {
        JMXConnector jmxc = null;
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

            System.out.println("[...] Connecting with JMX server");
            while (jmxc == null) {
                try {
                    jmxc = JMXConnectorFactory.connect(url, null);
                } catch (IOException ex) {
                    Thread.sleep(50);
                }
            }
            System.out.println("[OK] Connected with JMX server");

            MBeanServerConnection connection = jmxc.getMBeanServerConnection();

            List<MetricProvider> providers = getProviders(connection);

            beforeExecution(providers);

            connected();

            long startTime = TimingUtil.getMillis();
            long iteration = 1;
            while (true) {
                long currentTime = TimingUtil.getMillis();
                long toWait = iteration * resolutionMilliseconds - (currentTime - startTime);
                if (toWait > 0) {
                    Thread.sleep(toWait);
                }

                List<Double> values = new ArrayList<>(providers.size());
                double seconds = TimingUtil.millisSince(startTime) * Unit.getScaleFactor(Unit.MILLISECOND, Unit.SECOND);
                values.add(seconds);

                for (MetricProvider provider : providers) {
                    values.add(provider.getCurrentValue());
                }

                for (MetricsListener listener : listeners) {
                    listener.metricsUpdate(values);
                }

                iteration++;
            }


        } catch (InterruptedException ex) {
            // Ignore
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jmxc != null) {
            try {
                jmxc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        listeners.forEach(MetricsListener::close);

        System.out.println("Metrics finished");
    }

    private void connected() {
        connectionLock.lock();
        try {
            connected.set(true);
            connectedCondition.signalAll();
        } finally {
            connectionLock.unlock();
        }
    }

    public void blockUntilConnected() throws InterruptedException {
        connectionLock.lock();
        try {
            while (!connected.get()) {
                connectedCondition.await();
            }
        } finally {
            connectionLock.unlock();
        }
    }
}
