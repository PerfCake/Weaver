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

import org.perfcake.util.ObjectFactory;

import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * A worker that can periodically switch between other workers.
 * There is only one configuration parameter. Other parameters are in the formof workerXX_&lt;worker property&gt;.
 * For each of the underlying workers, there must be one mandatory property configured - workerXX_class - that specifies
 * the worker class name. By default the package org.perfcake.examples.weaver.worker is assumed.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class SwitchingWorker implements Worker, MapConfigurable {

   /**
    * Logger.
    */
   private static Logger log = LogManager.getLogger(SwitchingWorker.class);

   /**
    * List of underlying workers that are switched periodically.
    */
   private List<Worker> workers = new ArrayList<>();

   /**
    * Period counter.
    */
   private AtomicInteger counter = new AtomicInteger(0);

   /**
    * How often switch the worker.
    */
   private int switchPeriod = 1000;

   @Override
   public void work(final RoutingContext context) throws Exception {
      if (workers.size() > 0) {
         final Worker w = workers.get(counter.getAndIncrement() % switchPeriod % workers.size());
         w.work(context);
      }
   }

   @Override
   public boolean configure(final Properties configuration) {
      final Map<Integer, Properties> configurations = new HashMap<>();
      final LongAdder errorCount = new LongAdder();

      // parse configurations
      configuration.forEach((k, v) -> {
         final String key = k.toString();
         final String val = v.toString();

         if (key.startsWith("worker")) {
            int workerNumber = Integer.parseInt(key.substring(6, key.indexOf("_")));
            configurations.computeIfAbsent(workerNumber, n -> new Properties());

            configurations.get(workerNumber).put(key.substring(key.indexOf("_") + 1), val);
         }
      });

      // create workers
      configurations.forEach((n, props) -> {
         String clazz = props.getProperty("class");
         clazz = clazz.contains(".") ? clazz : "org.perfcake.examples.weaver.worker." + clazz;

         try {
            final Worker worker = (Worker) ObjectFactory.summonInstance(clazz, props);

            boolean add = true;
            if (worker instanceof MapConfigurable) {
               add = ((MapConfigurable) worker).configure(props);
            }

            if (add) {
               workers.add(worker);
            } else {
               log.error("Unable to configure underlying worker no. " + n);
               errorCount.increment();
            }
         } catch (ReflectiveOperationException e) {
            log.error("Unable to configure underlying worker no. " + n + ": ", e);
            errorCount.increment();
         }
      });

      return errorCount.intValue() == 0 && workers.size() > 0;
   }

   public int getSwitchPeriod() {
      return switchPeriod;
   }

   public void setSwitchPeriod(final int switchPeriod) {
      this.switchPeriod = switchPeriod;
   }

   List<Worker> getWorkers() {
      return workers;
   }
}
