package pl.edu.agh.kafkaload.kafkametrics.providers;

import pl.edu.agh.kafkaload.kafkametrics.MetricProvider;
import pl.edu.agh.kafkaload.util.NumbersConverter;
import pl.edu.agh.kafkaload.util.TimingUtil;
import pl.edu.agh.kafkaload.util.Unit;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ErrorRateProvider implements MetricProvider {
    private final String name;
    private final MBeanServerConnection connection;
    private long lastTime;
    private double lastCount;

    public ErrorRateProvider(String name, MBeanServerConnection connection) {
        this.name = name;
        this.connection = connection;
    }

    private double getCount() throws Exception {
        try {
            Set<ObjectName> names = connection.queryNames(
                    new ObjectName("kafka.network:type=RequestMetrics,name=ErrorsPerSec,*"),
                    null
            );

            List<ObjectName> errorObjectNames = names.stream()
                    .filter(name -> !"NONE".equalsIgnoreCase(name.getKeyProperty("error")))
                    .collect(Collectors.toList());

            double count = 0.0;

            for (ObjectName errorObjectName : errorObjectNames) {
                Attribute attribute = (Attribute) connection.getAttributes(errorObjectName, new String[]{"Count"}).get(0);
                double objectCount = NumbersConverter.convert(attribute.getValue());
                count += objectCount;
            }

            return count * 10.0;
        } catch (InstanceNotFoundException ex) {
            return 0.0;
        }
    }

    @Override
    public double getCurrentValue() throws Exception {
        double currentCount = getCount();
        long currentTime = TimingUtil.getMillis();

        long timeDiff = currentTime - lastTime;
        double countDiff = currentCount - lastCount;

        lastTime = currentTime;
        lastCount = currentCount;

        if (countDiff < 0.0 || timeDiff <= 0.0) {
            return 0.0;
        }

        return countDiff / (timeDiff * Unit.getScaleFactor(Unit.MILLISECOND, Unit.SECOND));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void start() throws Exception {
        lastCount = getCount();
        lastTime = TimingUtil.getMillis();
    }
}
