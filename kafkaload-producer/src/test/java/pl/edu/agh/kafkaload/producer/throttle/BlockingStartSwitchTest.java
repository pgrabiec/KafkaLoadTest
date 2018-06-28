package pl.edu.agh.kafkaload.producer.throttle;

import org.junit.Test;

import static org.junit.Assert.*;

public class BlockingStartSwitchTest {

    @Test(timeout = 100)
    public void testStart() throws Exception {
        StartSwitch startSwitch = new BlockingStartSwitch();

        assertFalse(startSwitch.testIfStarted());

        startSwitch.start();

        assertTrue(startSwitch.testIfStarted());

        startSwitch.awaitStart();
    }
}