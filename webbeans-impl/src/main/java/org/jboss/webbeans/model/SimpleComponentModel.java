package org.jboss.webbeans.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.Initializer;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;

public class SimpleComponentModel<T> extends AbstractComponentModel<T>
{
   
   public static final String LOGGER_NAME = "componentMetaModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private SimpleConstructor<T> constructor;
   private InjectableMethod<?> removeMethod;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   @SuppressWarnings("unchecked")
   public SimpleComponentModel(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem, container);
      this.constructor = initConstructor(getType());
      // TODO Interceptors
   }
   
   @SuppressWarnings("unchecked")
   protected static <T> SimpleConstructor<T> initConstructor(Class<? extends T> type)
   {
      if (type.getConstructors().length == 1)
      {
         Constructor<T> constructor = type.getConstructors()[0];
         log.finest("Exactly one constructor (" + constructor +") defined, using it as the component constructor for " + type);
         return new SimpleConstructor<T>(constructor);
      }
      
      if (type.getConstructors().length > 1)
      {
         List<Constructor<T>> initializerAnnotatedConstructors = Reflections.getConstructors(type, Initializer.class);
         List<Constructor<T>> bindingTypeAnnotatedConstructors = Reflections.getConstructorsForMetaAnnotatedParameter(type, BindingType.class);
         log.finest("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + type);
         log.finest("Found " + bindingTypeAnnotatedConstructors + " with parameters annotated with binding types for " + type);
         if ((initializerAnnotatedConstructors.size() + bindingTypeAnnotatedConstructors.size()) > 1)
         {
            if (initializerAnnotatedConstructors.size() > 1)
            {
               throw new RuntimeException("Cannot have more than one constructor annotated with @Initializer for " + type);
            }
            
            else if (bindingTypeAnnotatedConstructors.size() > 1)
            {
               throw new RuntimeException("Cannot have more than one constructor with binding types specified on constructor parameters for " + type);
            }
            else
            {
               throw new RuntimeException("Specify a constructor either annotated with @Initializer or with parameters annotated with binding types for " + type);
            }
         }
         else if (initializerAnnotatedConstructors.size() == 1)
         {
            Constructor<T> constructor = initializerAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the component constructor for " + type);
            return new SimpleConstructor<T>(constructor);
         }
         else if (bindingTypeAnnotatedConstructors.size() == 1)
         {
            Constructor<T> constructor = bindingTypeAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") with parameters annotated with binding types defined, using it as the component constructor for " + type);
            return new SimpleConstructor<T>(constructor);
         }
      }
      
      if (type.getConstructors().length == 0)
      {      
         Constructor<T> constructor = (Constructor<T>) Reflections.getConstructor(type);
         log.finest("No constructor defined, using implicit no arguement constructor for " + type);
         return new SimpleConstructor<T>(constructor);
      }
      
      throw new RuntimeException("Cannot determine constructor to use for " + type);
   }

   public SimpleConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   public InjectableMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

   
}
