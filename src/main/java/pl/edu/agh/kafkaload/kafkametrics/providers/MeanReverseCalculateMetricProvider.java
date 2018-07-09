package pl.edu.agh.kafkaload.kafkametrics.providers;

import pl.edu.agh.kafkaload.util.NumbersConverter;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

public class MeanReverseCalculateMetricProvider extends AbstractMetricProvider {
    private double lastCount;
    private double lastMean;

    public MeanReverseCalculateMetricProvider(String objectName, String countAttribute, String meanAttribute, MBeanServerConnection connection, String name, Unit sourceUnit, Unit targetUnit) throws MalformedObjectNameException {
        super(objectName, new String[] {countAttribute, meanAttribute}, connection, name, sourceUnit, targetUnit);
    }

    @Override
    double fetchCurrentValue() throws Exception {
        AttributeList attributes = getAttributes();
        double currentCount = NumbersConverter.convert(((Attribute) attributes.get(0)).getValue());
        double currentMean = NumbersConverter.convert(((Attribute) attributes.get(1)).getValue());

        double countDiff = currentCount - lastCount;
        if (countDiff == 0.0) {
            return 0.0;
        }

        double meanSinceLast = (currentMean * currentCount - lastMean * lastCount) / countDiff;

        lastMean = currentMean;
        lastCount = currentCount;

        return meanSinceLast;
    }

    @Override
    public void start() throws Exception {
        AttributeList attributes = getAttributes();
        lastCount = NumbersConverter.convert(((Attribute) attributes.get(0)).getValue());
        lastMean = NumbersConverter.convert(((Attribute) attributes.get(1)).getValue());
    }
}
