/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.perfcake.examples.weaver;

import org.perfcake.examples.weaver.worker.Worker;
import org.perfcake.examples.weaver.worker.WorkerThread;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Weaver HTTP server.
 */
public final class WeaverServer {

   /**
    * The HTTP server.
    */
   private final HttpServer server;

   /**
    * The thread which runs the server.
    */
   private final Thread serverThread;

   /**
    * Queue of workers ready to process requests.
    */
   private final Queue<Worker> workers;

   /**
    * Thread pool executing the workers.
    */
   private final ThreadPoolExecutor executor;

   /**
    * Initializes and starts the HTTP server.
    *
    * @param workers
    *       Queue of workers.
    * @param executor
    *       Thread pool for executing the workers.
    * @param port
    *       Port where to listen.
    * @param host
    *       Host where to bind.
    */
   WeaverServer(final Queue<Worker> workers, final ThreadPoolExecutor executor, final int port, final String host) {
      this.workers = workers;
      this.executor = executor;
      final Vertx vertx = Vertx.vertx();
      server = vertx.createHttpServer();
      final Router router = Router.router(vertx);
      router.route().handler(BodyHandler.create());
      router.route().handler(this::handle);
      serverThread = new Thread(() -> server.requestHandler(router::accept).listen(port, host));
      serverThread.setDaemon(true);
      serverThread.setName("weaver-server");
      serverThread.start();
   }

   /**
    * Handles an incoming request and submits it for execution.
    *
    * @param context
    *       HTTP routing context.
    */
   private void handle(final RoutingContext context) {
      final WorkerThread workerThread = new WorkerThread(workers, context);
      executor.submit(workerThread);
   }

   /**
    * Stops the HTTP server.
    */
   void close() {
      server.close();
   }
}