package org.jboss.weld.resources;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.weld.resources.spi.ScheduledExecutorServiceFactory;

public class SingleThreadScheduledExecutorServiceFactory implements ScheduledExecutorServiceFactory
{

   private final ScheduledExecutorService executorService;

   public SingleThreadScheduledExecutorServiceFactory()
   {
      this.executorService = Executors.newScheduledThreadPool(1);
   }
   
   public ScheduledExecutorService get()
   {
      return executorService;
   }
   
   public void cleanup()
   {
      this.executorService.shutdown();
   }
   
}
