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

/**
 * Limits the maximal number of requests per second before it returns a bad response code.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class MaxSpeedWorker extends DelayWorker {

   /**
    * How quickly (calls per second) can this service be called before it returns bad code.
    */
   private int maxSpeed = 1000;

   /**
    * The error code to return
    */
   private int badCode = 404;

   /**
    * When the service was last called.
    */
   private long lastCalled = 0L;

   @Override
   public void work(final RoutingContext context) throws Exception {
      if (System.nanoTime() - lastCalled > 1_000_000_000d / maxSpeed) {
         super.work(context);
         lastCalled = System.nanoTime();
      } else {
         lastCalled = System.nanoTime();
         context.response().setStatusCode(badCode).end("bad bad bad");
      }
   }

   public int getMaxSpeed() {
      return maxSpeed;
   }

   public void setMaxSpeed(final int maxSpeed) {
      this.maxSpeed = maxSpeed;
   }

   public int getBadCode() {
      return badCode;
   }

   public void setBadCode(final int badCode) {
      this.badCode = badCode;
   }
}
