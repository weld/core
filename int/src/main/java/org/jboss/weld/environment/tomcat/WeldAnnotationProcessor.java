package org.jboss.weld.environment.tomcat;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.NamingException;

import org.apache.AnnotationProcessor;
import org.jboss.weld.manager.api.WeldManager;

public class WeldAnnotationProcessor implements AnnotationProcessor
{
   
   private final Map<Class<?>, InjectionTarget<?>> injectionTargets;
   
   private final WeldManager manager;
   
   public WeldAnnotationProcessor(WeldManager manager)
   {
      this.manager = manager;
      this.injectionTargets = new ConcurrentHashMap<Class<?>, InjectionTarget<?>>();
   }

   public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      // not data-race safe, however doesn't matter, as the injection target created for class A is interchangable for another injection target created for class A
      // TODO Make this a concurrent cache when we switch to google collections
      Class<?> clazz = instance.getClass();
      if (!injectionTargets.containsKey(clazz))
      {
         injectionTargets.put(clazz, manager.createInjectionTarget(manager.createAnnotatedType(clazz)));
      }
      CreationalContext<Object> cc = manager.createCreationalContext(null);
      InjectionTarget<Object> it = (InjectionTarget<Object>) injectionTargets.get(clazz);
      it.inject(instance, cc);
   }

   public void postConstruct(Object arg0) throws IllegalAccessException, InvocationTargetException
   {
      // TODO Auto-generated method stub
      
   }
   
   public void preDestroy(Object arg0) throws IllegalAccessException, InvocationTargetException
   {
      // TODO Auto-generated method stub
      
   }
   
}
