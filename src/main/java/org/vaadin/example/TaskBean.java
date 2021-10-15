package org.vaadin.example;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class TaskBean {

    @Resource
    private ManagedExecutorService mes;

    public void longRunningGreeting(Consumer<String> callback, String name) {
        mes.submit(new TimeSpendingTask(callback, name));
    }

    private long spendTime(int min, int var) {
        long timeWellSpent = TimeUnit.MILLISECONDS.convert(
                Math.round((Math.random() * (double) var + (double) min)),
                TimeUnit.SECONDS);

        try {
            Thread.sleep(timeWellSpent);
        } catch (InterruptedException e) {
            // All fine
        }

        return timeWellSpent;
    }

    private class TimeSpendingTask implements Runnable, ManagedTask {
        private final Logger LOG = Logger.getLogger(TimeSpendingTask.class.getName());

        private final Consumer<String> callback;
        private final String name;

        private String result;

        public TimeSpendingTask(Consumer<String> callback, String name) {
            this.callback = callback;
            this.name = name;
        }

        @Override
        public void run() {
            long t = spendTime(5, 15);
            result = "Hello " + name + "! t=" + t;
        }

        @Override
        public ManagedTaskListener getManagedTaskListener() {
            return new ManagedTaskListener() {
                @Override
                public void taskSubmitted(Future<?> future, ManagedExecutorService managedExecutorService, Object o) {
                    LOG.info("Submitted: future=" + future + " o=" + o);
                }

                @Override
                public void taskAborted(Future<?> future, ManagedExecutorService managedExecutorService, Object o, Throwable throwable) {
                    LOG.info("Aborted: future=" + future + " o=" + o + " t=" + throwable);
                }

                @Override
                public void taskDone(Future<?> future, ManagedExecutorService managedExecutorService, Object o, Throwable throwable) {
                    LOG.info("Done: future=" + future + " o=" + o + " t=" + throwable);
                    try {
                        callback.accept(result);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Failed: future=" + future + ": e", e);
                        e.printStackTrace();
                    }
                }

                @Override
                public void taskStarting(Future<?> future, ManagedExecutorService managedExecutorService, Object o) {
                    LOG.info("Starting: future=" + future + " o=" + o);
                }
            };
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            return Collections.emptyMap();
        }
    }
}
