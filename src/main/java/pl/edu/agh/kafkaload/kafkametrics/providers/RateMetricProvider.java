package pl.edu.agh.kafkaload.kafkametrics.providers;

import pl.edu.agh.kafkaload.util.NumbersConverter;
import pl.edu.agh.kafkaload.util.TimingUtil;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

public class RateMetricProvider extends AbstractMetricProvider {
    private static final double MILLIS_PER_SECOND = 1000.0;
    private double lastCount = 0.0;
    private long lastTime;

    public RateMetricProvider(String objectName, String attribute, MBeanServerConnection connection, String name, Unit sourceUnit, Unit targetUnit) throws MalformedObjectNameException {
        super(objectName, new String[]{attribute}, connection, name, sourceUnit, targetUnit);
    }

    @Override
    double fetchCurrentValue() throws Exception {
        double currentCount = getCurrentCount();
        long currentTime = TimingUtil.getMillis();

        long elapsed = currentTime - lastTime;
        if (elapsed == 0) {
            elapsed = 1;
        }

        double rate = (currentCount - lastCount) / ((double) elapsed / MILLIS_PER_SECOND);

        lastTime = currentTime;
        lastCount = currentCount;

        return rate;
    }

    @Override
    public void start() throws Exception {
        lastCount = getCurrentCount();
        lastTime = TimingUtil.getMillis();
    }

    private double getCurrentCount() throws Exception {
        Attribute attribute = (Attribute) getAttributes().get(0);
        return NumbersConverter.convert(attribute.getValue());
    }
}
