package pl.edu.agh.kafkaload;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class ThreadsManager {
    private final Queue<Future> futures = new LinkedList<>();
    private final ExecutorService executorService;

    public ThreadsManager(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setWorkers(int workersCount, Supplier<Runnable> runnableSupplier) {
        int diff = workersCount - futures.size();
        if (diff != 0) {
            if (diff > 0) {
                addWorkers(diff, runnableSupplier);
            } else {
                removeWorkers(Math.max(futures.size(), -diff));
            }
        }
    }

    private void removeWorkers(int count) {
        for (int i = 0; i < count; i++) {
            Future future = futures.remove();
            future.cancel(true);
        }
    }

    private void addWorkers(int count, Supplier<Runnable> runnableSupplier) {
        for (int i = 0; i < count; i++) {
            Future future = executorService.submit(runnableSupplier.get());
            futures.add(future);
        }
    }
}
