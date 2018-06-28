package pl.edu.agh.kafkaload.producer.throttle;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultLoadThrottleTest {

    @Test(timeout = 2000)
    public void testAwaitNextSend() throws Exception {
        testAwait(1000.0, 5.0);
        testAwait(10000.0, 10.0);
        testAwait(10.0, 1.01);
    }

    private void testAwait(double rate, double acceptanceFactor) throws InterruptedException {
        double interval = 1000000000.0 / rate;

        RateProvider rateProvider = mock(RateProvider.class);
        when(rateProvider.messageRateAt(anyLong())).thenReturn(rate);

        LoadThrottle throttle = new DefaultLoadThrottle(rateProvider);

        long startTime = System.nanoTime();

        throttle.start();
        throttle.awaitNextSend();

        long durationNano = System.nanoTime() - startTime;

        assertTrue((double) durationNano >= interval);
        assertTrue(durationNano <= interval * acceptanceFactor);
    }
}