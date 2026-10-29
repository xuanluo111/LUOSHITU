package com.github.catvod.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class Task {

    private static final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));
    private static final ListeningExecutorService largeExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(20));
    private static final ListeningExecutorService serialExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static ListeningExecutorService executor() {
        return executor;
    }

    public static ListeningExecutorService largeExecutor() {
        return largeExecutor;
    }

    public static ScheduledExecutorService scheduler() {
        return scheduler;
    }

    public static Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public static <T> ListenableFuture<T> submit(Callable<T> callable, long timeoutMillis) {
        SettableFuture<T> result = SettableFuture.create();
        CallTimeout timeout = new CallTimeout(timeoutMillis);
        ListenableFutureTask<T> task = ListenableFutureTask.create(() -> {
            Runnable restore = timeout.enter();
            try {
                return callable.call();
            } finally {
                restore.run();
            }
        });
        result.addListener(() -> {
            timeout.close();
            if (!task.isDone()) task.cancel(true);
        }, MoreExecutors.directExecutor());
        Futures.addCallback(task, callback(result::set, result::setException), MoreExecutors.directExecutor());
        timeout.start(() -> result.setException(new TimeoutException("Task exceeded " + timeoutMillis + " ms")));
        executor.execute(task);
        return result;
    }

    public static Future<?> submitLarge(Runnable task) {
        return largeExecutor.submit(task);
    }

    /**
     * 洛视频：不设超时的异步提交。submit* 系列的 timeout 语义是「到点就 cancel(true)」——
     * 对下载这类有副作用的长时间任务，中断会让收尾逻辑把已下的分片当成垃圾清掉，
     * 调用方只是不想一直等，不是不想让它下完。用这个提交，等多久由调用方另外控制。
     */
    public static <T> Future<T> submitDetached(Callable<T> callable) {
        return largeExecutor.submit(callable);
    }

    // 洛视频：大任务（如 jar 下载）与小的加载任务共池时，45s 超时预算会把排队时间也算进去，
    // 极端情况下全程耗在队列里 → 从未真正下载却误标失败。largeExecutor 重载避开排队。
    public static <T> ListenableFuture<T> submitLarge(Callable<T> callable, long timeoutMillis) {
        SettableFuture<T> result = SettableFuture.create();
        CallTimeout timeout = new CallTimeout(timeoutMillis);
        ListenableFutureTask<T> task = ListenableFutureTask.create(() -> {
            Runnable restore = timeout.enter();
            try {
                return callable.call();
            } finally {
                restore.run();
            }
        });
        result.addListener(() -> {
            timeout.close();
            if (!task.isDone()) task.cancel(true);
        }, MoreExecutors.directExecutor());
        Futures.addCallback(task, callback(result::set, result::setException), MoreExecutors.directExecutor());
        timeout.start(() -> result.setException(new TimeoutException("Task exceeded " + timeoutMillis + " ms")));
        largeExecutor.execute(task);
        return result;
    }

    public static void execute(Runnable task) {
        executor.execute(task);
    }

    public static void executeSerial(Runnable task) {
        serialExecutor.execute(task);
    }

    public static void schedule(Runnable task, long delay, TimeUnit unit) {
        scheduler.schedule(task, delay, unit);
    }

    public static <T> FutureCallback<T> callback(Consumer<T> onSuccess) {
        return callback(onSuccess, null);
    }

    public static <T> FutureCallback<T> callback(Consumer<T> onSuccess, @Nullable Consumer<Throwable> onFailure) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(T result) {
                onSuccess.accept(result);
            }

            @Override
            public void onFailure(@NonNull Throwable error) {
                if (onFailure != null) onFailure.accept(error);
            }
        };
    }
}
