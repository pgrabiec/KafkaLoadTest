package pl.edu.agh.kafkaload.producer.throttle;

import pl.edu.agh.kafkaload.api.TimingUtil;

public class DefaultLoadThrottle implements LoadThrottle {
    private final StartSwitch startSwitch = new BlockingStartSwitch();
    private final RateProvider rateProvider;
    private long startTime = Long.MAX_VALUE;
    private long lastSendOffset = 0;

    public DefaultLoadThrottle(RateProvider rateProvider) {
        this.rateProvider = rateProvider;
    }

    private long getCurrentTimeOffset() {
        return TimingUtil.nanoSince(startTime);
    }

    @Override
    public void awaitNextSend() throws InterruptedException {
        startSwitch.awaitStart();

        double rate = rateProvider.messageRateAt(getCurrentTimeOffset());
        double intervalNano = 1000000000.0 / rate;
        while (getCurrentTimeOffset() - lastSendOffset < intervalNano) {
            // Spin
        }

        lastSendOffset = getCurrentTimeOffset();
    }

    @Override
    public void start() {
        startSwitch.start();
        startTime = TimingUtil.getNanoTimestamp();
    }

    @Override
    public boolean finished() {
        return rateProvider.finished(getCurrentTimeOffset());
    }
}
