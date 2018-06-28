package pl.edu.agh.kafkaload.producer.throttle;

public interface LoadThrottle {
    /**
     * Returns when there is time to send new message
     * */
    void awaitNextSend() throws InterruptedException;

    /**
     * Starts the throttle (from the beginning of the characteristics
     * */
    void start();

    /**
     * @return true if there are no more messages to send
     * */
    boolean finished();
}
