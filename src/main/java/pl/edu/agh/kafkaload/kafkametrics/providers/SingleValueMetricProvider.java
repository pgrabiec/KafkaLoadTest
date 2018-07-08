package pl.edu.agh.kafkaload.kafkametrics.providers;

import pl.edu.agh.kafkaload.util.NumbersConverter;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

public class SingleValueMetricProvider extends AbstractMetricProvider {
    public SingleValueMetricProvider(String objectName, String attribute, MBeanServerConnection connection, String name, Unit sourceUnit, Unit targetUnit) throws MalformedObjectNameException {
        super(objectName, new String[] {attribute}, connection, name, sourceUnit, targetUnit);
    }

    @Override
    double fetchCurrentValue() throws Exception {
        return NumbersConverter.convert(((Attribute) getAttributes().get(0)).getValue());
    }
}
