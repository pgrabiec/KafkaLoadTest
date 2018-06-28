package pl.edu.agh.kafkaload.producer.throttle;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingStartSwitch implements StartSwitch {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Lock startLock = new ReentrantLock();
    private final Condition startCondition = startLock.newCondition();

    private boolean localStarted = false;

    @Override
    public void start() {
        started.set(true);
        startLock.lock();
        try {
            startCondition.signalAll();
        } finally {
            startLock.unlock();
        }
    }

    @Override
    public void awaitStart() throws InterruptedException {
        if (!localStarted) {
            while (!started.get()) {
                startLock.lock();
                try {
                    startCondition.await();
                } finally {
                    startLock.unlock();
                }
            }
            localStarted = true;
        }
    }

    @Override
    public boolean testIfStarted() {
        return started.get();
    }
}
