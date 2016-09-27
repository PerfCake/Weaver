Thread Weaver
=============

_An ill-behaved microservices emulator for testing purposes._

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

## Documentation

Weaver is a tool for developers, the best documentation are the sources
and JavaDoc. Anybody volunteering in creating a Wiki please let me know
by logging an issue!

## Running

To obtain a binary distribution, use Maven as follows:

```
$ mvn package assembly:single -DskipTests
```

Now you can find the binary distribution in `target` directory in various compressed
formats. Uncompress any of them in a directory and run with `bin/weaver.[sh|bat]` depending
on your environment.

There are sample configuration files in the `projects` directory.

## Building

When you want to work directly with source distribution you have the following options.

To compile the application run:

```
$ mvn package -DskipTests 
```

To start weaver run:

```
$ mvn exec:exec -Dweaver.config=<configuration file> [-Dweaver.threads=<thread number>] [-Dweaver.shuffle=<true|false>]
```

## Source code

Weaver follows git-flow approach with `devel` as the main development branch.
In the `master` branch, there is always a stable version.