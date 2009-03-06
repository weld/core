package org.jboss.webbeans.injection;

import java.util.Set;
import java.util.concurrent.Callable;

import javax.servlet.Servlet;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.ConcurrentCache;

public class ServletInjector
{
   
   private final ConcurrentCache<Class<?>, Set<FieldInjectionPoint<?>>> servlets;
   private final ManagerImpl manager;

   public ServletInjector(ManagerImpl manager)
   {
      this.manager = manager;
      this.servlets = new ConcurrentCache<Class<?>, Set<FieldInjectionPoint<?>>>();
   }   
   
   public void inject(final Servlet instance)
   {
      Set<FieldInjectionPoint<?>> injectionPoints = servlets.putIfAbsent(instance.getClass(), new Callable<Set<FieldInjectionPoint<?>>>()
      {
         
         public Set<FieldInjectionPoint<?>> call() throws Exception
         {
            return Beans.getFieldInjectionPoints(AnnotatedClassImpl.of(instance.getClass()), null);
         }
         
      }
      );
      for (FieldInjectionPoint<?> injectionPoint : injectionPoints)
      {
         injectionPoint.inject(instance, manager, null);
      }
   }
   
}
