package com.ukom.sample;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    //每组任务并发执行，组内任务依次执行
    @Test
    public void executeTaskGroupSync() throws InterruptedException {
        List<List<Task>> taskLists = createTasks();
        for (List<Task> tasks : taskLists) {
            Executor executor = getExecutor();
            executor.execute(() -> {
                for (Task task : tasks) {
                    task.run();
                }
            });
        }

        Thread.sleep(1000);
    }

    //组内任务并发执行，组间同次序任务依次执行
    @Test
    public void executeSameIndexTaskSync() throws InterruptedException {
        List<List<Task>> taskLists = createTasks();
        Executor executor = getExecutor();
        for (int i = 0; i < 3; i++) {
            final int index = i;
            executor.execute(() -> {
                for (List<Task> tasks : taskLists) {
                    tasks.get(index).run();
                }
            });
        }

        Thread.sleep(1000);
    }

    /**
     * 每一组任务顺序执行，但是所有组的同次序任务执行完成之后，才能执行下一个次序的任务，即保证任务次序的执行顺序：
     * [A1, B1, C1](无序，并发执行) -> [A2, B2, C2](无序，并发执行)  -> [A3, B3, C3](无序，并发执行)
     */
    @Test
    public void executeIndexTaskInOrder() throws InterruptedException {
        List<List<Task>> taskLists = createTasks();
        Executor executor = getExecutor();
        CyclicBarrier barrier = new CyclicBarrier(3);
        for (List<Task> tasks : taskLists) {
            executor.execute(() -> {
                try {
                    for (Task task : tasks) {
                        task.run();
                        barrier.await();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        Thread.sleep(1000);
    }

    private Executor getExecutor() {
        return Executors.newFixedThreadPool(3);
    }

    private List<List<Task>> createTasks() {
        List<Task> aList = Arrays.asList(new Task("A1"), new Task("A2"), new Task("A3"));
        List<Task> bList = Arrays.asList(new Task("B1"), new Task("B2"), new Task("B3"));
        List<Task> cList = Arrays.asList(new Task("C1"), new Task("C2"), new Task("C3"));
        return Arrays.asList(aList, bList, cList);
    }

    private static class Task implements Runnable {
        private String name;

        public Task(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(new Random().nextInt(100));
                System.out.println(String.format("Task[%s] has done on %s ",
                        name, Thread.currentThread().toString()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


/********************************************* UPDATE ********************************************/

    //组内任务并发执行，组间依次执行，要求 A 组内 3 个都执行完，再执行 B 组，B 组都执行完再执行 C 组
    @Test
    public void executeTaskGroupsInOrder() throws InterruptedException {
        List<List<Task>> taskLists = createTasks();
        Executor executor = getExecutor();
        for (List<Task> tasks : taskLists) {
            CountDownLatch latch = new CountDownLatch(tasks.size());
            executeTasksWithLatch(tasks, executor, latch);
            latch.await();
        }
    }

    public void executeTasksWithLatch(List<Task> tasks, Executor executor, CountDownLatch latch){
        for (Task task : tasks){
            executor.execute(() -> {
                task.run();
                latch.countDown();
            });
        }
    }

}