package pl.edu.agh.kafkaload.producer.throttle;

public interface StartSwitch {
    void start();

    void awaitStart() throws InterruptedException;

    boolean testIfStarted();
}
