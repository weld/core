package org.jboss.webbeans.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.Initializer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;

public class SimpleComponentModel<T> extends AbstractClassComponentModel<T>
{
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private SimpleConstructor<T> constructor;

   private String location;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   @SuppressWarnings("unchecked")
   public SimpleComponentModel(AnnotatedType annotatedItem, AnnotatedType xmlAnnotatedItem, ManagerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem);
      init(container);
   }
   
   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      initConstructor();
      checkType(getType());
      initInjectionPoints();
      // TODO Interceptors
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (InjectableParameter<?> injectable : constructor.getParameters())
      {
         injectionPoints.add(injectable);
      }
   }
   
   public static void checkType(Class<?> type)
   {
      if (type.isMemberClass())
      {
         throw new RuntimeException("Simple Web Bean " + type + " cannot be an inner class");
      }
   }
   
   @SuppressWarnings("unchecked")
   protected void initConstructor()
   {
      if (getType().getConstructors().length == 1)
      {
         Constructor<T> constructor = (Constructor<T>) getType().getConstructors()[0];
         log.finest("Exactly one constructor (" + constructor +") defined, using it as the component constructor for " + getType());
         this.constructor = new SimpleConstructor<T>(constructor);
         return;
      }
      
      if (getType().getConstructors().length > 1)
      {
         List<Constructor<T>> initializerAnnotatedConstructors = Reflections.getConstructors(getType(), Initializer.class);
         List<Constructor<T>> bindingTypeAnnotatedConstructors = Reflections.getConstructorsForMetaAnnotatedParameter(getType(), BindingType.class);
         log.finest("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + getType());
         log.finest("Found " + bindingTypeAnnotatedConstructors + " with parameters annotated with binding types for " + getType());
         if ((initializerAnnotatedConstructors.size() + bindingTypeAnnotatedConstructors.size()) > 1)
         {
            if (initializerAnnotatedConstructors.size() > 1)
            {
               throw new RuntimeException("Cannot have more than one constructor annotated with @Initializer for " + getType());
            }
            
            else if (bindingTypeAnnotatedConstructors.size() > 1)
            {
               throw new RuntimeException("Cannot have more than one constructor with binding types specified on constructor parameters for " + getType());
            }
            else
            {
               throw new RuntimeException("Specify a constructor either annotated with @Initializer or with parameters annotated with binding types for " + getType());
            }
         }
         else if (initializerAnnotatedConstructors.size() == 1)
         {
            Constructor<T> constructor = initializerAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the component constructor for " + getType());
            this.constructor = new SimpleConstructor<T>(constructor);
            return;
         }
         else if (bindingTypeAnnotatedConstructors.size() == 1)
         {
            Constructor<T> constructor = bindingTypeAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") with parameters annotated with binding types defined, using it as the component constructor for " + getType());
            this.constructor = new SimpleConstructor<T>(constructor);
            return;
         }
      }
      
      if (getType().getConstructors().length == 0)
      {      
         Constructor<T> constructor = (Constructor<T>) Reflections.getConstructor(getType());
         log.finest("No constructor defined, using implicit no arguement constructor for " + getType());
         this.constructor = new SimpleConstructor<T>(constructor);
         return;
      }
      
      throw new RuntimeException("Cannot determine constructor to use for " + getType());
   }

   public SimpleConstructor<T> getConstructor()
   {
      return constructor;
   }
   

   @Override
   public String toString()
   {
      return "SimpleComponentModel[" + getType().getName() + "]";
   }
   
   @Override
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Simple Component; declaring class: " + getType() +";";
      }
      return location;
   }
   
}
