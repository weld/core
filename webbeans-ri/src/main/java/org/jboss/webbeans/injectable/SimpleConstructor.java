package org.jboss.webbeans.injectable;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.util.LoggerUtil;

public class SimpleConstructor<T> extends Invokable<T, AnnotatedConstructor<T>> implements BeanConstructor<T, Constructor<T>> 
{
   
   public static final String LOGGER_NAME = "beanConstructor";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);

   private AnnotatedConstructor<T> constructor;
   
   public SimpleConstructor(AnnotatedConstructor<T> constructor)
   {
      super(constructor.getParameters());
      this.constructor = constructor;
      log.finest("Initialized metadata for " + constructor);
   }

   public T invoke(ManagerImpl manager)
   {
      return invoke(manager, null, getParameterValues(manager));
   }

   @Override
   public T invoke(Manager manager, Object instance, Object[] parameters)
   {
      try
      {
         log.finest("Creating new instance of " + constructor);
         return constructor.getAnnotatedConstructor().newInstance(parameters);
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Error instantiating " + constructor, e);
      }
   }

   @Override
   public T invoke(ManagerImpl manager, Object instance)
   {
      return invoke(manager, instance, getParameterValues(manager));
   }
   
   @Override
   public AnnotatedConstructor<T> getAnnotatedItem()
   {
      return constructor;
   }
   
}
