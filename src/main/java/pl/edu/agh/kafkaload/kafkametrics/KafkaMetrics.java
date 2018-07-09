package pl.edu.agh.kafkaload.kafkametrics;

import pl.edu.agh.kafkaload.kafkametrics.providers.RateMetricProvider;
import pl.edu.agh.kafkaload.kafkametrics.providers.SingleValueMetricProvider;
import pl.edu.agh.kafkaload.kafkametrics.providers.Unit;
import pl.edu.agh.kafkaload.util.TimingUtil;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KafkaMetrics implements Runnable {
    private final List<MetricsListener> listeners;
    private final long resolutionMilliseconds;

    public KafkaMetrics(List<MetricsListener> listeners, long resolutionMilliseconds) {
        this.listeners = listeners;
        this.resolutionMilliseconds = resolutionMilliseconds;
    }

    private void beforeExecution(List<MetricProvider> providers) throws Exception {
        for (MetricProvider provider : providers) {
            provider.start();
        }

        List<String> names = new ArrayList<>(providers.size() + 1);
        names.add("time_s");
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
                        "input_MB/s",
                        Unit.BYTE,
                        Unit.MEGABYTE
                ),

                new RateMetricProvider(
                        "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec",
                        "Count",
                        connection,
                        "output_MB/s",
                        Unit.BYTE,
                        Unit.MEGABYTE
                ),

                new SingleValueMetricProvider(
                        "kafka.network:type=RequestChannel,name=RequestQueueSize",
                        "Value",
                        connection,
                        "request_queue_size",
                        Unit.UNIT,
                        Unit.UNIT
                )
        );
    }

    @Override
    public void run() {
        JMXConnector jmxc = null;
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

            jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection connection = jmxc.getMBeanServerConnection();

            List<MetricProvider> providers = getProviders(connection);

            beforeExecution(providers);

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

        System.out.println("Metrics finished");
    }
}
