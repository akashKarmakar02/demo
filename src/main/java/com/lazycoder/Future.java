package com.lazycoder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Future<T> {

    private final FutureTask<T> futureTask;

    private Future(Callable<T> callable) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        this.futureTask = new FutureTask<>(callable);
        executor.submit(futureTask::run);
    }

    private Future(Runnable runnable) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        this.futureTask = new FutureTask<>(runnable, null); // null for void return
        executor.submit(futureTask);
    }

    public static <T> Future<T> run(Callable<T> callable) {
        return new Future<>(callable);
    }

    public static Future run(Runnable runnable) {
        return new Future(runnable);
    }

    public T await() throws InterruptedException, ExecutionException {
        if (!futureTask.isDone()) {
            futureTask.get(); // May throw exceptions
        }
        return futureTask.get(); // Only for methods with return type
    }

    public String toString() {
        return "Future: " + futureTask;
    }
}
