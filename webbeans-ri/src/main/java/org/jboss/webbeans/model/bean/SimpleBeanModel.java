package org.jboss.webbeans.model.bean;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;

import javax.webbeans.DefinitionException;
import javax.webbeans.Initializer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;

public class SimpleBeanModel<T> extends AbstractClassBeanModel<T>
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
   public SimpleBeanModel(AnnotatedType<T> annotatedItem, AnnotatedType<T> xmlAnnotatedItem, ManagerImpl container)
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
      if (Reflections.isNonStaticInnerClass(type))
      {
         throw new DefinitionException("Simple Web Bean " + type + " cannot be a non-static inner class");
      }
      if (Reflections.isParameterizedType(type))
      {
         throw new DefinitionException("Simple Web Bean " + type + " cannot be a parameterized type");
      }
   }
   
   protected void initConstructor()
   {
      
      List<Constructor<T>> initializerAnnotatedConstructors = Reflections.getAnnotatedConstructors(getType(), Initializer.class);
      log.finest("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + getType());
      if (initializerAnnotatedConstructors.size() > 1)
      {
         if (initializerAnnotatedConstructors.size() > 1)
         {
            throw new DefinitionException("Cannot have more than one constructor annotated with @Initializer for " + getType());
         }
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         Constructor<T> constructor = initializerAnnotatedConstructors.get(0);
         log.finest("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the bean constructor for " + getType());
         this.constructor = new SimpleConstructor<T>(constructor);
         return;
      }
         
      Constructor<T> emptyConstructor = Reflections.getConstructor(getType());
      if (emptyConstructor != null)
      {
         log.finest("Exactly one constructor (" + constructor +") defined, using it as the bean constructor for " + getType());
         this.constructor = new SimpleConstructor<T>(emptyConstructor);
         return;
      }
      
      throw new DefinitionException("Cannot determine constructor to use for " + getType());
   }

   public SimpleConstructor<T> getConstructor()
   {
      return constructor;
   }
   

   @Override
   public String toString()
   {
      return "SimpleWebBean[" + getAnnotatedItem().toString() + "]";
   }
   
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Simple Bean; declaring class: " + getType() +";";
      }
      return location;
   }

   @Override
   protected AbstractClassBeanModel<? extends T> getSpecializedType()
   {
      //TODO: lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if ( superclass!=null )
      {
         return new SimpleBeanModel(new SimpleAnnotatedType(superclass), getEmptyAnnotatedType(superclass), container);
      }
      else {
         throw new RuntimeException();
      }
   }


}
