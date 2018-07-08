package pl.edu.agh.kafkaload.kafkametrics.providers;

import pl.edu.agh.kafkaload.kafkametrics.MetricProvider;

import javax.management.*;
import java.io.IOException;

public abstract class AbstractMetricProvider implements MetricProvider {
    private final ObjectName objectName;
    private final String[] attributes;
    private final MBeanServerConnection connection;
    private final String name;
    private final Unit sourceUnit;
    private final Unit targetUnit;

    public AbstractMetricProvider(String objectName, String[] attributes, MBeanServerConnection connection, String name, Unit sourceUnit, Unit targetUnit)
            throws MalformedObjectNameException {
        this.objectName = new ObjectName(objectName);
        this.attributes = attributes;
        this.connection = connection;
        this.name = name;
        this.sourceUnit = sourceUnit;
        this.targetUnit = targetUnit;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public final double getCurrentValue() throws Exception {
        return fetchCurrentValue() * Unit.getScaleFactor(sourceUnit, targetUnit);
    }

    protected AttributeList getAttributes() throws InstanceNotFoundException, IOException, ReflectionException {
        return connection.getAttributes(objectName, attributes);
    }

    abstract double fetchCurrentValue() throws Exception;
}
