package org.jcp.forkjoin.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * https://en.wikipedia.org/wiki/Merge_sort#Parallel_merge_sort
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
 */
public class ParallelMergeSort {

    public static <T extends Comparable<? super T>> List<T> sort(final List<T> list) {

        if (list == null || list.size() < 2) {
            return list;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        forkJoinPool.invoke(new ForkJoinMergeSort<>(list, 0, list.size()));

        return list;
    }


    private static class ForkJoinMergeSort<T extends Comparable<? super T>> extends RecursiveAction {

        private final List<T> list;
        private final int high;
        private final int low;

        ForkJoinMergeSort(final List<T> list, final int low, final int high) {
            this.list = list;
            this.low = low;
            this.high = high;
        }

        void merge(final int middle) {
            final Comparator<T> comparator = Comparator.naturalOrder();
            final List<T> buffer = new ArrayList<>(list.subList(low, high));
            int i = low;
            int j = middle + 1;
            int k = high;
            while (i <= middle && j <= high) {
                if (comparator.compare(buffer.get(i), buffer.get(j)) <= 0) {
                    list.set(k, buffer.get(i));
                    i++;
                } else {
                    list.set(k, buffer.get(j));
                    j++;
                }
                k++;
            }
            while (i <= middle) {
                list.set(k, buffer.get(i));
                k++;
                i++;
            }
        }


        @Override
        protected void compute() {
            if (high - low <= 1) {
                return;
            }

            int middle = (high + low) / 2;

            invokeAll(new ForkJoinMergeSort<>(list, low, middle), new ForkJoinMergeSort<>(list, middle + 1, high));

            merge(middle);
        }
    }
}
