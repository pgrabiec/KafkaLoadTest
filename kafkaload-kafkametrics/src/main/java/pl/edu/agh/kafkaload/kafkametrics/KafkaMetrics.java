package pl.edu.agh.kafkaload.kafkametrics;

import com.yammer.metrics.reporting.JmxReporter;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

public class KafkaMetrics implements Runnable {
    @Override
    public void run() {
        JMXConnector jmxc = null;
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

            jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            ObjectName messageRate = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec");
            JmxReporter.MeterMBean messageRateProxy = JMX.newMBeanProxy(mbsc, messageRate, JmxReporter.MeterMBean.class, true);

            ObjectName purgatorySize = new ObjectName("kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=Produce");
            JmxReporter.GaugeMBean purgatorySizeProxy = JMX.newMBeanProxy(mbsc, purgatorySize, JmxReporter.GaugeMBean.class, true);

//            kafka.network:type=RequestMetrics,name=RequestQueueTimeMs,request=Produce

            while (true) {
                System.out.println("Purgatory = " + purgatorySizeProxy.getValue());
                System.out.println("Load = " + messageRateProxy.getCount());
                Thread.sleep(300);
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
    }

}
