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
package org.perfcake.examples.weaver.worker;

import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;

/**
 * A thread processing an incoming request using a worker.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class WorkerThread implements Runnable {

   private static final Logger log = LogManager.getLogger(WorkerThread.class);

   /**
    * Queue of workers to process the requests.
    */
   private final Queue<Worker> workers;

   /**
    * Incoming HTTP routing context.
    */
   private final RoutingContext context;

   /**
    * Creates a new thread instance.
    *
    * @param workers
    *       Queue of workers to poll from.
    * @param context
    *       Incoming HTTP routing context.
    */
   public WorkerThread(final Queue<Worker> workers, final RoutingContext context) {
      this.workers = workers;
      this.context = context;
   }

   @Override
   public void run() {
      final Worker w = workers.poll();

      try {
         w.work(context);
      } catch (Throwable t) {
         log.error("Error processing request: ", t);
      }

      workers.offer(w);
   }
}
