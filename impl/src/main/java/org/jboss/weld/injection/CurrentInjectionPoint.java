package org.jboss.weld.injection;

import java.util.Stack;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bootstrap.api.Service;

public class CurrentInjectionPoint implements Service
{
   
   private final ThreadLocal<Stack<InjectionPoint>> currentInjectionPoint;
   
   public CurrentInjectionPoint()
   {
      this.currentInjectionPoint = new ThreadLocal<Stack<InjectionPoint>>()
      {
         @Override
         protected Stack<InjectionPoint> initialValue()
         {
            return new Stack<InjectionPoint>();
         }
      };
   }
      
   /**
    * Replaces (or adds) the current injection point. If a current injection 
    * point exists, it will be replaced. If no current injection point exists, 
    * one will be added.
    * 
    * @param injectionPoint the injection point to use
    * @return the injection point added, or null if previous existed did not exist
    */
   public void push(InjectionPoint injectionPoint)
   {
      currentInjectionPoint.get().push(injectionPoint);
   }
   
   public InjectionPoint pop()
   {
      return currentInjectionPoint.get().pop();
   }
   
   /**
    * The injection point being operated on for this thread
    * 
    * @return the current injection point
    */
   public InjectionPoint peek()
   {
      if (!currentInjectionPoint.get().empty())
      {
         return currentInjectionPoint.get().peek();
      }
      else
      {
         return null;
      }
   }
   
   public void pushDummy()
   {
      currentInjectionPoint.get().push(DummyInjectionPoint.INSTANCE);
   }
   
   public void popDummy()
   {
      if (!currentInjectionPoint.get().isEmpty() && DummyInjectionPoint.INSTANCE.equals(currentInjectionPoint.get().peek()))
      {
         currentInjectionPoint.get().pop();
      }
   }

   public void cleanup()
   {
      this.currentInjectionPoint.remove();
   }

}
