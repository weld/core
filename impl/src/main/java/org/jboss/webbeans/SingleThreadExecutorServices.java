/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.webbeans.manager.api.ExecutorServices;

/**
 * @author pmuir
 *
 */
public class SingleThreadExecutorServices implements ExecutorServices
{
   
   private transient final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
   

   /**
    * Provides access to the executor service used for asynchronous tasks.
    * 
    * @return the ExecutorService for this manager
    */
   public ExecutorService getTaskExecutor()
   {
      return taskExecutor;
   }

   public void cleanup()
   {
      taskExecutor.shutdown();
      try
      {
         // Wait a while for existing tasks to terminate
         if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS))
         {
            taskExecutor.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS))
            {
               // Log the error here
            }
         }
      }
      catch (InterruptedException ie)
      {
         // (Re-)Cancel if current thread also interrupted
         taskExecutor.shutdownNow();
         // Preserve interrupt status
         Thread.currentThread().interrupt();
      }
   }

}
