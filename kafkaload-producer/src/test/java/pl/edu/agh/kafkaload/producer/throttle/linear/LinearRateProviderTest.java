package pl.edu.agh.kafkaload.producer.throttle.linear;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class LinearRateProviderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNoPoints() throws Exception {
        new LinearRateProvider(Collections.emptyList(), 1024);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnePoint() throws Exception {
        new LinearRateProvider(Collections.singletonList(new LoadPoint(0, 2.0)), 1024);
    }

    @Test
    public void testMessageRateAt() throws Exception {
        testRateCalculation(
                Arrays.asList(
                        new LoadPoint(3500, 2000000.0),
                        new LoadPoint(0, 0.0),
                        new LoadPoint(3000, 1000000.0),
                        new LoadPoint(1000, 1000000.0),
                        new LoadPoint(4000, 0.0)
                ),
                Arrays.asList(
                        new LoadPoint(0, 0.0),
                        new LoadPoint(500, 500000.0),
                        new LoadPoint(1000, 1000000.0),
                        new LoadPoint(2000, 1000000.0),
                        new LoadPoint(3000, 1000000.0),
                        new LoadPoint(3250, 1500000.0),
                        new LoadPoint(3500, 2000000.0),
                        new LoadPoint(3750, 1000000.0),
                        new LoadPoint(4000, 0.0)
                )
        );
    }

    private void testRateCalculation(List<LoadPoint> points, Collection<LoadPoint> loadPoints) {
        LinearRateProvider provider = new LinearRateProvider(points, 1024);

        for (LoadPoint loadPoint : loadPoints) {
            assertTrue(Math.abs(provider.messageRateAt(loadPoint.getX()) - loadPoint.getY()) < 0.0001);
        }
    }
}