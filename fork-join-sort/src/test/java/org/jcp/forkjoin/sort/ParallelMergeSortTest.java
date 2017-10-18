package org.jcp.forkjoin.sort;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ParallelMergeSortTest {

    @Test
    public void test() throws Exception {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(ThreadLocalRandom.current().nextInt());
        }

        System.out.println("In");

        list.forEach(System.out::println);

        list = ParallelMergeSort.sort(list);

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));

        System.out.println("Out");

        list.forEach(System.out::println);
    }

}