package org.jcp.forkjoin.sort;

import java.util.List;

public class ParallelMergeSort {

    public static <T> List<T> sort(final List<T> list) {
        if (list == null || list.size() < 2) {
            return list;
        }

        return new ForkJoinMergeSort<>(list).sort();
    }


    private static class ForkJoinMergeSort<T> {

        private final List<T> list;

        ForkJoinMergeSort(final List<T> list) {
            this.list = list;
        }

        List<T> sort() {

            throw new UnsupportedOperationException("Not implemented yet...");
        }


    }
}
