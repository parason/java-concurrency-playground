package org.jcp.forkjoin.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * https://en.wikipedia.org/wiki/Merge_sort#Parallel_merge_sort
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
 */
public class ParallelMergeSort {

    /**
     * A short-cut for {@link #sort(List, Comparator, int)} below with the {@link Comparator#naturalOrder()} as the comparator
     * and Runtime.getRuntime().availableProcessors() parallelism level
     *
     * @param list to be sorted
     * @param <T>  type of elements in the list
     * @return sorted list
     */
    public static <T extends Comparable<? super T>> List<T> sort(final List<T> list) {
        return sort(list, Comparator.naturalOrder(), Runtime.getRuntime().availableProcessors());
    }

    /**
     * A short-cut for {@link #sort(List, Comparator, int)} below with the Runtime.getRuntime().availableProcessors()
     * parallelism level
     *
     * @param list       to be sorted
     * @param comparator to be used while sorting to determine the element order
     * @param <T>        type of elements in the list
     * @return sorted list
     */
    public static <T extends Comparable<? super T>> List<T> sort(final List<T> list, final Comparator<T> comparator) {
        return sort(list, comparator, Runtime.getRuntime().availableProcessors());
    }

    /**
     * A short-cut for {@link #sort(List, Comparator, int)} below with the {@link Comparator#naturalOrder()} as the comparator
     *
     * @param list        to be sorted
     * @param parallelism level of the Fork-Join Framework
     * @param <T>         type of elements in the list
     * @return sorted list
     */
    public static <T extends Comparable<? super T>> List<T> sort(final List<T> list, final int parallelism) {
        return sort(list, Comparator.naturalOrder(), parallelism);
    }

    /**
     * Does the soring trick
     *
     * @param list        to be sorted
     * @param comparator  to be used while sorting to determine the element order
     * @param parallelism level of the Fork-Join Framework
     * @param <T>         type of elements in the list
     * @return sorted list
     */
    public static <T extends Comparable<? super T>> List<T> sort(final List<T> list,
            final Comparator<T> comparator,
            final int parallelism) {

        if (list == null || list.size() < 2) {
            return list;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);

        forkJoinPool.invoke(new ForkJoinMergeSort<>(list, comparator, 0, list.size()));

        return list;
    }


    /**
     * A {@link RecursiveAction} implementation that does the trick
     *
     * @param <T> type of elements to be sorted
     */
    private static class ForkJoinMergeSort<T extends Comparable<? super T>> extends RecursiveAction {

        private final List<T>       list;
        private final Comparator<T> comparator;
        private final int           high;
        private final int           low;


        /**
         * Creates a new instance with
         *
         * @param list       to be sorted
         * @param comparator to be used for element order determination
         * @param low        lower list bound
         * @param high       upper list bound
         */
        ForkJoinMergeSort(final List<T> list, final Comparator<T> comparator, final int low, final int high) {
            this.list = list;
            this.comparator = comparator;
            this.low = low;
            this.high = high;
        }

        /**
         * "Sorts" everything back together
         */
        void merge() {
            final List<T> buffer          = Collections.unmodifiableList(new ArrayList<>(list.subList(low, high)));
            final int     bufferLastIndex = buffer.size() - 1;
            final int     bufferMiddle    = (high - low) / 2;
            // re-arrange items in the list according to buffer
            for (int listIndex = low, bufferLeftIndex = 0, bufferRightIndex = bufferMiddle; listIndex < high; listIndex++) {
                if (bufferRightIndex > bufferLastIndex ||
                        (bufferLeftIndex < bufferMiddle &&
                                comparator.compare(buffer.get(bufferLeftIndex), buffer.get(bufferRightIndex)) <= 0)) {
                    list.set(listIndex, buffer.get(bufferLeftIndex++));
                } else {
                    list.set(listIndex, buffer.get(bufferRightIndex++));
                }
            }
        }

        /**
         * Splits the list until single element and merges everything back
         */
        @Override
        protected void compute() {
            // consider as sorted
            if (high - low <= 1) {
                return;
            }
            // get the middle index
            int middle = low + ((high - low) / 2);
            // recursive split
            invokeAll(new ForkJoinMergeSort<>(list, comparator, low, middle), new ForkJoinMergeSort<>(list, comparator, middle, high));
            // put everything back together
            merge();
        }
    }
}
