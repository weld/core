package org.jboss.weld.test.unit.context;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplictionScopedObject
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
