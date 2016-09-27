Thread Weaver
=============

A sample HTTP based service which creates a pool of threads and serves the 
incoming requests using various types of workers.

The workers can for example delay for some time, or return different HTTP response codes etc.

The types of workers and their parameters can be configured in a provided configuration file.
The format of the file is as as follows:

```
10x WorkerClassName=attribute1:value1,attribute2:value2,...
1x AnotherWorker=...
2x WorkerWithoutAttributes
...
```

The first number tells the multiplicity (how many times in a row the worker should be created) followed by an _x_ letter.
Then the class name of the worker (the package if not present defaults to _org.perfcake.weaver.worker_).

You can keep the worker in the order as specified in the configuration file or shuffle them by setting _weaver.shuffle_ property
to true.

You can limit the number of threads by setting _weaver.thread_ property. 
All the threads are started as daemon threads not to block the process when terminated.
The number of threads can never be higher than the number of workers created.

The application can be terminated by ^C / Ctrl+C.

To compile the application run:

```
$ mvn package -DskipTests 
```

To start weaver run:

```
$ mvn exec:exec -Dweaver.config=<configuration file> [-Dweaver.threads=<thread number>] [-Dweaver.shuffle=<true|false>]
```
