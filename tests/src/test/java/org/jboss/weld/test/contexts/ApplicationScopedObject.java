package org.jboss.weld.test.contexts;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationScopedObject
{
   private AtomicInteger counter = new AtomicInteger();

   public void increment()
   {
      counter.incrementAndGet();
   }
   public int getValue()
   {
      return counter.get();
   }
}
