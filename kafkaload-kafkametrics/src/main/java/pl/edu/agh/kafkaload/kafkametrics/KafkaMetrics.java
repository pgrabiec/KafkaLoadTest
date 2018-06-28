package pl.edu.agh.kafkaload.kafkametrics;

import com.yammer.metrics.reporting.JmxReporter;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaMetrics implements Runnable {
    private final Writer writer;
    private final AtomicBoolean finished;

    public KafkaMetrics(Writer writer, AtomicBoolean finished) {
        this.writer = writer;
        this.finished = finished;
    }

    @Override
    public void run() {
        //columns in csv file
//        List<MetricsParam> params = new ArrayList<>();

        JMXConnector jmxc = null;
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

            jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            // Init proxies for metrics
            ObjectName bytesInName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec");
            JmxReporter.MeterMBean bytesInProxy = JMX.newMBeanProxy(mbsc, bytesInName, JmxReporter.MeterMBean.class, true);

            ObjectName queueSizeName = new ObjectName("kafka.network:type=RequestChannel,name=RequestQueueSize");
            JmxReporter.GaugeMBean queueSizeProxy = JMX.newMBeanProxy(mbsc, queueSizeName, JmxReporter.GaugeMBean.class, true);

            ObjectName bytesOutName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec");
            JmxReporter.MeterMBean bytesOutProxy = JMX.newMBeanProxy(mbsc, bytesOutName, JmxReporter.MeterMBean.class, true);

            long lastBytesIn = bytesInProxy.getCount();
            long bytesIn;
            long lastBytesInTime = System.currentTimeMillis();
            long bytesInTime;

            long lastBytesOut = bytesOutProxy.getCount();
            long bytesOut;
            long lastBytesOutTime = System.currentTimeMillis();
            long bytesOutTime;

            long startTime = System.currentTimeMillis();

            while (!finished.get()) {
                Thread.sleep(50);

                bytesIn = bytesInProxy.getCount();
                bytesInTime = System.currentTimeMillis();
                double bytesInPerSec = (bytesIn - lastBytesIn) / ((bytesInTime - lastBytesInTime) / 1000.0);
                lastBytesIn = bytesIn;
                lastBytesInTime = bytesInTime;

                bytesOut = bytesOutProxy.getCount();
                bytesOutTime = System.currentTimeMillis();
                double bytesOutPerSec = (bytesOut - lastBytesOut) / ((bytesOutTime - lastBytesOutTime) / 1000.0);
                lastBytesOut = bytesOut;
                lastBytesOutTime = bytesOutTime;

                writer.write(String.join(
                        " ",
                        Long.toString(System.currentTimeMillis() - startTime),
                        Long.toString(Math.round(bytesInPerSec)),
                        Objects.toString(queueSizeProxy.getValue()),
                        Long.toString(Math.round(bytesOutPerSec))
                ));
                writer.write('\n');
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (jmxc != null) {
                try {
                    jmxc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

//        try {
//            new StatefulBeanToCsvBuilder(writer)
//                    .withSeparator(' ')
//                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
//                    .build()
//                    .write(params);
//            writer.flush();
//        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
//            e.printStackTrace();
//        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finished");
    }

}
