package pl.edu.agh.kafkaload.producer.throttle;

public interface RateProvider {
    /**
     * @return messages per second at the given time
     * */
    double messageRateAt(long time);

    boolean finished(long time);

    int messageSize();
}
