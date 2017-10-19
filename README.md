# java-concurrency
java concurrency experiments

* producer-consumer:

 This is an implementation of a Producer - Consumer pattern, where the Consumer schedules the produced items for ansync processing.
 An example implementation can be found in tests.

* pipeline (in progress):

 An implementation of a concurrent pipeline flow pattern. The flow consists of a number of operations that represent "atomic" parts of a process.
 The operations can be executed concurrently and joined at the end of the process. Inspired by http://www.informit.com/articles/article.aspx?p=366887&seqNum=8

* fork-join-sort:

 A parallel Mergesort implementation (Java Fork-Join Framework)
