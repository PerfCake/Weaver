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

import java.util.Properties;

/**
 * Interface for workers with special configuration needs so that they can get the whole configuration map for their own processing.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface MapConfigurable {

   /**
    * Configure the worker from the given configuration map.
    *
    * @param configuration
    *       Configuration properties.
    * @return True iff it was possible to successfully configure the worker and it should be added to the common pool of workers.
    */
   boolean configure(final Properties configuration);
}
