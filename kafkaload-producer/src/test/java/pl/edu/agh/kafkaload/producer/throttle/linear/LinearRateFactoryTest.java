package pl.edu.agh.kafkaload.producer.throttle.linear;

import org.junit.Test;
import pl.edu.agh.kafkaload.producer.throttle.RateProvider;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LinearRateFactoryTest {
    @Test
    public void testCreateProviders() throws Exception {
        String conf = "3\n" +
                "1024\n" +
                "0 0.0\n" +
                "1000 1000000\n" +
                "3000 1000000\n" +
                "3500 2000000";
        LinearRateFactory factory = new LinearRateFactory();

        List<RateProvider> providers = factory.createProviders(new StringReader(conf));

        assertEquals(3, providers.size());
        for (RateProvider provider : providers) {
            assertTrue(Math.abs(provider.messageRateAt(1000 * 1000000) - (1000000.0 / 3)) < 0.0001);
        }
    }
}