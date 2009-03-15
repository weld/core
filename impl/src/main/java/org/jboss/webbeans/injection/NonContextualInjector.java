package org.jboss.webbeans.injection;

import java.util.Set;
import java.util.concurrent.Callable;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.collections.ConcurrentCache;

public class NonContextualInjector
{
   
   private final ConcurrentCache<Class<?>, Set<FieldInjectionPoint<?>>> instances;
   private final ManagerImpl manager;

   public NonContextualInjector(ManagerImpl manager)
   {
      this.manager = manager;
      this.instances = new ConcurrentCache<Class<?>, Set<FieldInjectionPoint<?>>>();
   }   
   
   public void inject(final Object instance)
   {
      Set<FieldInjectionPoint<?>> injectionPoints = instances.putIfAbsent(instance.getClass(), new Callable<Set<FieldInjectionPoint<?>>>()
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
