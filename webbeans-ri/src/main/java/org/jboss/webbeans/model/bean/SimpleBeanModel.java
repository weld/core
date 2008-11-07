package org.jboss.webbeans.model.bean;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.webbeans.DefinitionException;
import javax.webbeans.Initializer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;

public class SimpleBeanModel<T> extends AbstractClassBeanModel<T>
{
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private static Set<Class<?>> NO_ARGUMENTS = Collections.emptySet();
   
   private SimpleConstructor<T> constructor;
   private AnnotatedMethod<Object> postConstruct;
   private AnnotatedMethod<Object> preDestroy;

   private String location;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   public SimpleBeanModel(AnnotatedClass<T> annotatedItem, AnnotatedClass<T> xmlAnnotatedItem, ManagerImpl container)
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
      initPostConstruct();
      initPreDestroy();
      // TODO Interceptors
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (AnnotatedParameter<Object> parameter : constructor.getAnnotatedItem().getParameters())
      {
         injectionPoints.add(new InjectableParameter<Object>(parameter));
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
      Set<AnnotatedConstructor<T>> initializerAnnotatedConstructors = getAnnotatedItem().getAnnotatedConstructors(Initializer.class);
      log.finest("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + getType());
      if (initializerAnnotatedConstructors.size() > 1)
      {
         throw new DefinitionException("Cannot have more than one constructor annotated with @Initializer for " + getType());
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         this.constructor = new SimpleConstructor<T>(initializerAnnotatedConstructors.iterator().next());
         log.finest("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the bean constructor for " + getType());
         return;
      }
      else if (getAnnotatedItem().getConstructor(NO_ARGUMENTS) != null)
      {
         
         this.constructor = new SimpleConstructor<T>(getAnnotatedItem().getConstructor(NO_ARGUMENTS));
         log.finest("Exactly one constructor (" + constructor +") defined, using it as the bean constructor for " + getType());
         return;
      }
      else {
         throw new DefinitionException("Cannot determine constructor to use for " + getType());
      }
   }
   
   protected void initPostConstruct()
   {
      Set<AnnotatedMethod<Object>> postConstructMethods = getAnnotatedItem().getAnnotatedMethods(PostConstruct.class);
      log.finest("Found " + postConstructMethods + " constructors annotated with @Initializer for " + getType());
      if (postConstructMethods.size() > 1)
      {
         //TODO: actually this is wrong, in EJB you can have @PostConstruct methods on the superclass,
         //      though the Web Beans spec is silent on the issue
         throw new DefinitionException("Cannot have more than one post construct method annotated with @Initializer for " + getType());
      }
      else if (postConstructMethods.size() == 1)
      {
         this.postConstruct = postConstructMethods.iterator().next();
         log.finest("Exactly one post construct method (" + postConstruct +") for " + getType());
        return;
      }
   }

   protected void initPreDestroy()
   {
      Set<AnnotatedMethod<Object>> preDestroyMethods = getAnnotatedItem().getAnnotatedMethods(PreDestroy.class);
      log.finest("Found " + preDestroyMethods + " constructors annotated with @Initializer for " + getType());
      if (preDestroyMethods.size() > 1)
      {
         //TODO: actually this is wrong, in EJB you can have @PreDestroy methods on the superclass,
         //      though the Web Beans spec is silent on the issue
         throw new DefinitionException("Cannot have more than one pre destroy method annotated with @Initializer for " + getType());
      }
      else if (preDestroyMethods.size() == 1)
      {
         this.preDestroy = preDestroyMethods.iterator().next();
         log.finest("Exactly one post construct method (" + preDestroy +") for " + getType());
        return;
      }
   }

   public SimpleConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   public AnnotatedMethod<Object> getPostConstruct() 
   {
      return postConstruct;
   }
   

   public AnnotatedMethod<Object> getPreDestroy() 
   {
      return preDestroy;
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
         return new SimpleBeanModel(new SimpleAnnotatedClass(superclass), null, manager);
      }
      else {
         throw new RuntimeException();
      }
   }


}
