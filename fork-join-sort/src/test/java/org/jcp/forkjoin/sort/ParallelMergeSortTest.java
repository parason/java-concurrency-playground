package org.jcp.forkjoin.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

public class ParallelMergeSortTest {

    @Test
    public void testBasic() throws Exception {
        final List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            list.add(ThreadLocalRandom.current().nextInt());
        }

        final List<Integer> expected = new ArrayList<>(list);
        Collections.sort(expected);

        ParallelMergeSort.sort(list);

        Assert.assertEquals(expected, list);
    }

    @Test
    public void testOneElement() {
        final List<Integer> list = Collections.singletonList(1);
        ParallelMergeSort.sort(list);
        Assert.assertEquals(list, Collections.singletonList(1));
    }

    @Test
    public void testTwoElements() {
        List<Integer>       list     = Arrays.asList(2, 1);
        final List<Integer> expected = Arrays.asList(1, 2);
        list = ParallelMergeSort.sort(list);
        Assert.assertEquals(list, expected);
    }

}