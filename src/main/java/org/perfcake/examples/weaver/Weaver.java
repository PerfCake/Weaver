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

import org.perfcake.examples.weaver.worker.MapConfigurable;
import org.perfcake.examples.weaver.worker.Worker;
import org.perfcake.util.ObjectFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Main class to start the Weaver server according to the provided configuration.
 */
public final class Weaver {

   /**
    * My logger.
    */
   private static final Logger log = LogManager.getLogger(Weaver.class);

   @Parameter(names = { "-t", "--threads" }, description = "Number of threads, 0 = automatic based on number of workers")
   private int threads = 0;

   @Parameter(names = { "-s", "--shuffle" }, description = "Shuffle the workers", arity = 1)
   private boolean shuffle = false;

   @Parameter(names = { "-c", "--config" }, description = "Configuration file", required = true)
   private String config = null;

   @Parameter(names = { "--help" }, description = "Prints out command usage")
   private boolean help = false;

   @Parameter(names = { "-p", "--port" }, description = "Network port to listen on")
   private int port = 8080;

   @Parameter(names = { "-h", "--host" }, description = "Network host to listen on")
   private String host = "localhost";

   /**
    * Workers processing requests.
    */
   private final Queue<Worker> workers = new ConcurrentLinkedQueue<>();

   /**
    * Thread pool executing the workers.
    */
   private ThreadPoolExecutor executor;

   /**
    * Starts the server.
    *
    * @param args
    *       Command line arguments.
    * @throws IOException
    *       When it was not possible to parse the configuration.
    */
   public static void main(String[] args) throws IOException {
      final Weaver weaver = new Weaver();
      final JCommander jCommander = new JCommander(weaver, args);

      if (weaver.config == null || weaver.help) {
         jCommander.usage("weaver");
         System.exit(1);
      }

      if (!(new File(weaver.config).exists())) {
         System.out.println("The specified configuration file '" + weaver.config + "' does not exists.");
         System.exit(2);
      }

      weaver.init();
      weaver.run();
   }

   /**
    * Initializes the configuration.
    *
    * @throws IOException
    *       When it was not possible to parse the configuration.
    */
   public void init() throws IOException {
      int maxThreads = Files.lines(Paths.get(config)).collect(Collectors.summingInt(this::parseWorker));
      if (threads > maxThreads || threads == 0) {
         if (threads > maxThreads) {
            log.warn("Maximum possible threads is " + maxThreads + ", while you requested " + threads + ". Using " + maxThreads + ".");
         }
         threads = maxThreads;
      }

      if (shuffle) {
         log.info("Shuffling workers...");

         final List<Worker> workersList = workers.stream().collect(Collectors.toList());
         Collections.shuffle(workersList);
         workers.clear();
         workersList.forEach(workers::offer);
      }

      log.info("Creating executor with " + threads + " threads.");

      executor = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("worker-thread-%d").build());
   }

   /**
    * Parses worker configuration line.
    *
    * @param configLine
    *       The configuration line.
    * @return The number of worker instances created.
    */
   private int parseWorker(final String configLine) {
      if (configLine != null && !configLine.isEmpty() && !configLine.startsWith("#")) {
         final String[] spaceSplit = configLine.split(" ", 2);
         final String[] equalsSplit = spaceSplit[1].split("=", 2);
         final int count = Integer.parseInt(StringUtils.strip(spaceSplit[0], " x"));
         String clazz = StringUtils.strip(equalsSplit[0]);
         clazz = clazz.contains(".") ? clazz : "org.perfcake.examples.weaver.worker." + clazz;
         final String[] propertiesConfig = StringUtils.stripAll(StringUtils.strip(equalsSplit[1]).split(","));
         final Properties properties = new Properties();
         for (final String property : propertiesConfig) {
            final String[] keyValue = StringUtils.stripAll(property.split(":", 2));
            properties.setProperty(keyValue[0], keyValue[1]);
         }

         try {
            log.info("Summoning " + count + " instances of " + clazz + " with properties " + properties);
            for (int i = 0; i < count; i++) {
               final Worker worker = (Worker) ObjectFactory.summonInstance(clazz, properties);

               boolean add = true;
               if (worker instanceof MapConfigurable) {
                  log.info("Found auto-configurable worker, it is safe to ignore previous warnings about configuring workerX_ properties.");
                  add = ((MapConfigurable) worker).configure(properties);
               }

               if (add) {
                  workers.add(worker);
               } else {
                  log.warn("Bad configuration. Skipping worker " + clazz);
               }
            }

            return count;
         } catch (ReflectiveOperationException e) {
            log.error("Unable to parse line '" + configLine + "': ", e);
         }
      }

      return 0;
   }

   /**
    * Starts the server.
    */
   public void run() {
      final WeaverServer server = new WeaverServer(workers, executor, port, host);
      log.info("Started server listening on " + host + ":" + port);
      log.info("Press Ctrl+C to terminate...");
      try {
         System.in.read();
      } catch (IOException ioe) {
         log.error("Unable to read standard input: ", ioe);
      } finally {
         server.close();
      }
   }

   public int getThreads() {
      return threads;
   }

   public void setThreads(final int threads) {
      this.threads = threads;
   }

   public boolean isShuffle() {
      return shuffle;
   }

   public void setShuffle(final boolean shuffle) {
      this.shuffle = shuffle;
   }

   public String getConfig() {
      return config;
   }

   public void setConfig(final String config) {
      this.config = config;
   }

   public int getPort() {
      return port;
   }

   public void setPort(final int port) {
      this.port = port;
   }

   public String getHost() {
      return host;
   }

   public void setHost(final String host) {
      this.host = host;
   }

   public Queue<Worker> getWorkers() {
      return workers;
   }
}