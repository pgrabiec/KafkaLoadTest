package pl.edu.agh.kafkaload.kafkametrics;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.yammer.metrics.reporting.JmxReporter;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.BufferedWriter;
import java.io.IOException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaMetrics implements Runnable {
    @Override
    public void run() {

        //file with metrics
        String file = "src/main/java/pl.edu.agh.kafkaload.kafkametrics/data.csv";
        Path myPath = Paths.get(file);

        //columns in csv file
        List<MetricsParam> params= new ArrayList<>();

        JMXConnector jmxc = null;
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

            jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            ObjectName messageRate = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec");
            JmxReporter.MeterMBean messageRateProxy = JMX.newMBeanProxy(mbsc, messageRate, JmxReporter.MeterMBean.class, true);

            //metric for message conversion time
            //ObjectName messageConversion = new ObjectName("kafka.network:type=RequestMetrics,name=MessageConversionsTimeMs,request={Produce|Fetch}");
            //JmxReporter.GaugeMBean messageConversionProxy = JMX.newMBeanProxy(mbsc, messageConversion, JmxReporter.GaugeMBean.class, true);

            ObjectName purgatorySize = new ObjectName("kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=Produce");
            JmxReporter.GaugeMBean purgatorySizeProxy = JMX.newMBeanProxy(mbsc, purgatorySize, JmxReporter.GaugeMBean.class, true);

//            kafka.network:type=RequestMetrics,name=RequestQueueTimeMs,request=Produce

            while (true) {

             /*   int time = 0;
                int m1 = (int)purgatorySizeProxy.getValue();
                int m2 = (int)(long)messageRateProxy.getCount();
                int m3 = 3;
             */
                params.add(new MetricsParam(0,(int)(long)messageRateProxy.getCount(),(int)purgatorySizeProxy.getValue(),3));
                //System.out.println("Purgatory = " + purgatorySizeProxy.getValue());
                //System.out.println("Conversion = " + messageConversionProxy.getValue());
                //System.out.println("Load = " + messageRateProxy.getCount());
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

        try (BufferedWriter writer = Files.newBufferedWriter(myPath,
                StandardCharsets.UTF_8)) {

            StatefulBeanToCsv<MetricsParam> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            beanToCsv.write(params);

        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException |
                IOException ex) {
            Logger.getLogger(KafkaMetrics.class.getName()).log(
                    Level.SEVERE, ex.getMessage(), ex);
        }


    }

}
