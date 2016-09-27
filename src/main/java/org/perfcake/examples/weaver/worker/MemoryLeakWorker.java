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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Causes a memory leak by putting HTTP requests' bodies to a hash map using a dirty custom object without a proper
 * {@link #equals(Object)} and {@link #hashCode()} methods implementation.
 *
 * The hash map key (the register) name where the body is put can be specified by the {@link #keyHeader}.
 * The worker will look into the request's header to find the register name. If the specified header
 * is not found in the request a default register name will be used instead.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 * @author <a href="mailto:pavel.macik@gmail.com">Pavel Macík</a>
 */
public class MemoryLeakWorker extends NormalWorker {

   /**
    * Name of the HTTP header where a register key is specified.
    */
   private String keyHeader = "register";

   /**
    * A register that stores message bodies.
    */
   private Map<BadKey, String> register = Collections.synchronizedMap(new HashMap<BadKey, String>());

   @Override
   public void work(final RoutingContext context) throws Exception {
      super.work(context);
      final String registerKey = context.request().getHeader(getKeyHeader());
      final BadKey badKey = new BadKey(registerKey != null ? registerKey : "defaultRegister");
      register.put(badKey, context.getBodyAsString());
   }

   /**
    * Get the name of the HTTP header where a register key is specified.
    *
    * @return The header name.
    */
   public String getKeyHeader() {
      return keyHeader;
   }

   /**
    * Set the HTTP header that will contain the register name to where the message body is to be put.
    *
    * @param keyHeader
    *       The name of the header.
    */
   public void setKeyHeader(final String keyHeader) {
      this.keyHeader = keyHeader;
   }

   /**
    * A dirty custom hash map key implementation.
    * Without a proper {@link #equals(Object)} and {@link #hashCode()} methods implementation the hash map
    * will not be able to recognize an existing key and always create a new record.
    */
   private class BadKey {
      public final String key;

      public BadKey(String key) {
         this.key = key;
      }
   }
}
