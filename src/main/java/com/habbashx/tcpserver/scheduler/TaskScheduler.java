package com.habbashx.tcpserver.scheduler;


import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class for scheduling and managing synchronous and asynchronous tasks
 * with both delayed and repeating behaviors. Tasks are identified by unique IDs
 * which can be used to cancel them individually.
 *
 * This class operates on two separate thread pools:
 * 1. A single-threaded pool for synchronous tasks.
 * 2. A multi-threaded pool (up to 4 threads) for asynchronous tasks.
 *
 **/
public class TaskScheduler {

    /**
     * A thread-safe, single-threaded scheduled executor service used for executing synchronous tasks.
     * This executor ensures that tasks are executed in a sequential manner, maintaining synchronization.
     * It supports delayed and repeating task scheduling in the context of the task scheduler logic.
     *
     * The `syncTaskPool` is used internally by the scheduling mechanism to handle synchronous task execution,
     * ensuring consistent execution order and thread safety for tasks designated as synchronous.
     *
     * Tasks submitted to this pool are typically managed by the main thread of the scheduler, preventing
     * concurrency issues when interacting with shared resources.
     */
    private final ScheduledExecutorService syncTaskPool = Executors.newSingleThreadScheduledExecutor();
    /**
     * A scheduled thread pool used to manage asynchronous tasks.
     * This executor service allows scheduling and execution of delayed
     * or repeating tasks in a non-blocking manner, independent of the main thread.
     *
     * Commonly used for handling asynchronous operations within the
     * task scheduler, enabling efficient support for concurrent task execution.
     *
     * The pool size is set to 4 threads, providing a balance between
     * performance and resource usage.
     */
    private final ScheduledExecutorService asyncTaskPool = Executors.newScheduledThreadPool(4);
    /**
     * A thread-safe mapping of task IDs to their corresponding scheduled future tasks.
     * The task IDs are represented as Integer keys, and the ScheduledFuture objects represent the scheduled tasks.
     * This map is used to manage and track the lifecycle of scheduled tasks.
     * Tasks can be added, retrieved, or removed from the map as needed during runtime.
     */
    private final Map<Integer, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    /**
     * An atomic counter used for generating unique task IDs.
     * This ensures that each scheduled task within the TaskScheduler has a distinct identifier.
     * The counter starts at 0 and increments atomically with each new task.
     */
    private final AtomicInteger taskIdGenerator = new AtomicInteger(0);

    private boolean running = true;

    /**
     * Schedules a synchronous task to be executed after a specified delay in ticks.
     *
     * @param task The task to be executed. It must implement the Runnable interface.
     * @param delayInTicks The delay before the task is executed, specified in ticks (1 tick = 50 milliseconds).
     * @return A unique integer ID representing the scheduled task. This ID can be used to manage the task, such as canceling it.
     */
    public int runTaskLater(Runnable task, long delayInTicks) {
        return schedule(syncTaskPool, task, delayInTicks, 0);
    }

    /**
     * Schedules an asynchronous task to be executed after a specified delay in ticks.
     *
     * @param task The task to be executed. It must implement the Runnable interface.
     * @param delayInTicks The delay before the task is executed, specified in ticks (1 tick = 50 milliseconds).
     * @return A unique integer ID representing the scheduled task. This ID can be used to manage the task, such as canceling it.
     */
    public int runTaskAsyncLater(Runnable task, long delayInTicks) {
        return schedule(asyncTaskPool, task, delayInTicks, 0);
    }

    /**
     * Schedules a synchronous repeating task.
     */
    public int runTaskTimer(Runnable task, long delayInTicks, long periodInTicks) {
        return schedule(syncTaskPool, task, delayInTicks, periodInTicks);
    }

    /**
     * Schedules an asynchronous repeating task.
     */
    public int runTaskAsyncTimer(Runnable task, long delayInTicks, long periodInTicks) {
        return schedule(asyncTaskPool, task, delayInTicks, periodInTicks);
    }

    /**
     * Core task scheduler logic. Handles delayed and repeating tasks.
     */
    private int schedule(ScheduledExecutorService pool, Runnable task, long delayInTicks, long periodInTicks) {
        if (!running) throw new IllegalStateException("Task scheduler is not running.");

        int taskId = taskIdGenerator.incrementAndGet();
        long delayMs = ticksToMs(delayInTicks);
        long periodMs = periodInTicks > 0 ? ticksToMs(periodInTicks) : 0;

        ScheduledFuture<?> future;
        if (periodMs > 0) {
            future = pool.scheduleAtFixedRate(task, delayMs, periodMs, TimeUnit.MILLISECONDS);
        } else {
            future = pool.schedule(task, delayMs, TimeUnit.MILLISECONDS);
        }

        taskMap.put(taskId, future);
        return taskId;
    }

    /**
     * Cancels a task by its unique ID.
     */
    public void cancelTask(int taskId) {
        ScheduledFuture<?> future = taskMap.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * Shuts down the scheduler gracefully.
     */
    public void stopScheduler() {
        running = false;
        for (ScheduledFuture<?> future : taskMap.values()) {
            future.cancel(false);
        }
        taskMap.clear();
        syncTaskPool.shutdown();
        asyncTaskPool.shutdown();
    }

    /**
     * Converts ticks to milliseconds (1 tick = 50 milliseconds).
     */
    private long ticksToMs(long ticks) {
        return ticks * 50;
    }
}