package org.opengeo.data.importer.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class JobQueue {

    /** job id counter */
    AtomicLong counter = new AtomicLong();

    /** recent jobs */
    ConcurrentHashMap<Long,Task<?>> jobs = new ConcurrentHashMap<Long, Task<?>>();

    /** job runner */
    //ExecutorService pool = Executors.newCachedThreadPool();
    ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>()) {
        protected <T extends Object> RunnableFuture<T> newTaskFor(Callable<T> callable) {
            if (callable instanceof Job) {
                return new Task((Job) callable);
            }
            return super.newTaskFor(callable);
        };
        protected void afterExecute(Runnable r, Throwable t) {
            if (t != null && r instanceof Task) {
                ((Task)r).setError(t);
            }
        };
    };

    /** job cleaner */
    ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    {
        cleaner.scheduleAtFixedRate(new Runnable() {
            public void run() {
                List<Long> toremove = new ArrayList<Long>();
                for (Map.Entry<Long, Task<?>> e : jobs.entrySet()) {
                    if (e.getValue().isCancelled() || (e.getValue().isDone() && e.getValue().isRecieved())) {
                        toremove.add(e.getKey());
                    }
                }
                for (Long l : toremove) {
                    jobs.remove(l);
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public Long submit(Job<?> task) {
        Long jobid = counter.getAndIncrement();
        jobs.put(jobid, (Task) pool.submit(task));
        return jobid;
    }

    public Task<?> getFuture(Long jobid) {
        Task<?> t = jobs.get(jobid);
        t.recieve();
        return t;
    }

    public void shutdown() {
        cleaner.shutdownNow();
        pool.shutdownNow();
    }
}
