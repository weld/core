package org.jboss.webbeans.injectable;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedConstructor;
import org.jboss.webbeans.util.LoggerUtil;

public class SimpleConstructor<T> extends Unit<T, Constructor<T>> implements BeanConstructor<T>
{
   
   public static final String LOGGER_NAME = "beanConstructor";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);

   private AnnotatedConstructor<T> constructor;
   
   public SimpleConstructor(Constructor<T> constructor)
   {
      super(constructor.getParameterTypes(), constructor.getParameterAnnotations());
      this.constructor = new SimpleAnnotatedConstructor<T>(constructor);
      log.finest("Initialized metadata for " + constructor + " with injectable parameters " + getParameters());
   }

   public T invoke(ManagerImpl manager)
   {
      try
      {
         log.finest("Creating new instance of " + constructor.getType() + " with injected parameters " + getParameters());
         return constructor.getAnnotatedConstructor().newInstance(getParameterValues(manager));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Error instantiating " + constructor.getType(), e);
      }
   }
   
   @Override
   public AnnotatedItem<T, Constructor<T>> getAnnotatedItem()
   {
      return constructor;
   }
   
}
