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

import org.perfcake.examples.weaver.Weaver;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Queue;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class SwitchingWorkerTest {

   // Verifies correct configuration parsing and nested switching workers.
   @Test
   public void switchingWorkerConfigTest() throws IOException, URISyntaxException {
      final Weaver weaver = new Weaver();
      weaver.setConfig(new File(getClass().getResource("/").toURI()).getAbsolutePath() + "/switching.cfg");
      weaver.init();

      final Queue<Worker> q = weaver.getWorkers();
      SwitchingWorker w = (SwitchingWorker) q.poll();
      final List<Worker> workers = w.getWorkers();

      Assert.assertEquals(w.getSwitchPeriod(), 1010);
      Assert.assertEquals(workers.size(), 3);
      Assert.assertTrue(workers.get(0) instanceof NormalWorker);
      Assert.assertEquals(((NormalWorker) workers.get(0)).getStatusCode(), 333);
      Assert.assertTrue(workers.get(1) instanceof DelayWorker);
      Assert.assertEquals(((DelayWorker) workers.get(1)).getDelay(), 100);
      Assert.assertTrue(workers.get(2) instanceof SwitchingWorker);
      Assert.assertEquals(((SwitchingWorker) workers.get(2)).getWorkers().size(), 2);
   }

}