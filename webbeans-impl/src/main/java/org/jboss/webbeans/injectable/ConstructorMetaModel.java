package org.jboss.webbeans.injectable;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import javax.webbeans.Container;

import org.jboss.webbeans.util.LoggerUtil;

public class ConstructorMetaModel<T> extends UnitMetaModel<T>
{
   
   public static final String LOGGER_NAME = "componentConstructor";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);

   private Constructor<T> constructor;
   
   @SuppressWarnings("unchecked")
   public ConstructorMetaModel(Constructor<T> constructor)
   {
      super(constructor.getParameterTypes(), constructor.getParameterAnnotations());
      this.constructor = constructor;
      log.finest("Initialized metadata for " + constructor + " with injectable parameters " + getParameters());
   }
   
   public Constructor<T> getConstructor()
   {
      return constructor;
   }

   public T invoke(Container container)
   {
      try
      {
         log.finest("Creating new instance of " + constructor.getDeclaringClass() + " with injected parameters " + getParameters());
         return constructor.newInstance(getParameterValues(container));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Error instantiating " + constructor.getDeclaringClass(), e);
      }
   }
   
}
