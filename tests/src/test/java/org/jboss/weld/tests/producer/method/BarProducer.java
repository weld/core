package org.jboss.weld.tests.producer.method;

import java.lang.reflect.Member;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Class with a producer method and disposal method both containing InjectionPoint
 * parameters.
 * 
 * @author David Allen
 *
 */
public class BarProducer
{
   private static Bar disposedBar;
   private static Member disposedInjection;
   private static Member producedInjection;
   
   @Produces
   public Bar getBar(InjectionPoint injectionPoint)
   {
      producedInjection = injectionPoint.getMember();
      return new Bar("blah");
   }
   
   public void dispose(@Disposes @Any Bar bar, InjectionPoint injectionPoint)
   {
      disposedBar = bar;
      disposedInjection = injectionPoint.getMember();
   }

   public static Bar getDisposedBar()
   {
      return disposedBar;
   }

   public static Member getDisposedInjection()
   {
      return disposedInjection;
   }

   public static Member getProducedInjection()
   {
      return producedInjection;
   }
   
   public static void reset()
   {
      disposedBar = null;
      disposedInjection = null;
      producedInjection = null;
   }
}
