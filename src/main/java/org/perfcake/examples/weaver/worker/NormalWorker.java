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
 * Just processes the requests in an ordinary way. Can provide custom status code, status message, and response.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class NormalWorker implements Worker {

   /**
    * The status code to return.
    */
   private int statusCode = 200;

   /**
    * The status message to return. Null means that no status message is returned.
    */
   private String statusMessage = null;

   /**
    * Response to return. Null or empty string means that no response is returned unless mirrorRequest is set to true.
    * This response is ignore when mirrorRequest is set to true.
    */
   private String response = "";

   /**
    * When set to true, the response returned is the same as the original request body. Also, the response property is ignored when
    * this is set to true.
    */
   private boolean mirrorRequest = false;

   @Override
   public void work(final RoutingContext context) throws Exception {
      if (statusMessage != null) {
         context.response().setStatusMessage(statusMessage);
      }
      context.response().setStatusCode(statusCode);

      if (mirrorRequest) {
         context.response().end(context.getBodyAsString());
      } else if (response != null && !response.isEmpty()) {
         context.response().end(response);
      } else {
         context.response().end();
      }
   }

   public int getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(final int statusCode) {
      this.statusCode = statusCode;
   }

   public String getStatusMessage() {
      return statusMessage;
   }

   public void setStatusMessage(final String statusMessage) {
      this.statusMessage = statusMessage;
   }

   public String getResponse() {
      return response;
   }

   public void setResponse(final String response) {
      this.response = response;
   }

   public boolean isMirrorRequest() {
      return mirrorRequest;
   }

   public void setMirrorRequest(final boolean mirrorRequest) {
      this.mirrorRequest = mirrorRequest;
   }
}
