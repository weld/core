package org.jboss.webbeans.bean;

import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.webbeans.DefinitionException;
import javax.webbeans.Initializer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.impl.InjectableField;
import org.jboss.webbeans.introspector.impl.InjectableMethod;
import org.jboss.webbeans.introspector.impl.InjectableParameter;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;

public class SimpleBean<T> extends AbstractClassBean<T>
{
   
   private static LogProvider log = Logging.getLogProvider(SimpleBean.class);
   private static Set<Class<?>> NO_ARGUMENTS = Collections.emptySet();
   
   private AnnotatedConstructor<T> constructor;
   private String location;
   
   private AnnotatedMethod<Object> postConstruct;
   private AnnotatedMethod<Object> preDestroy;
   
   public SimpleBean(Class<T> type, ManagerImpl manager)
   {
      super(type, manager);
      init();
   }

   @Override
   public T create()
   {
      T instance = constructor.newInstance(getManager());
      bindDecorators();
      bindInterceptors();
      injectEjbAndCommonFields();
      injectBoundFields(instance);
      callInitializers(instance);
      callPostConstruct(instance);
      return instance;
   }
   
   @Override
   public void destroy(T instance)
   {
      callPreDestroy(instance);
   }
   
   protected void callPreDestroy(T instance)
   {
      AnnotatedMethod<Object> preDestroy = getPreDestroy();
      if (preDestroy!=null)
      {
         try
         {
            preDestroy.getAnnotatedMethod().invoke(instance);
         }
         catch (Exception e) 
         {
            throw new RuntimeException("Unable to invoke " + preDestroy + " on " + instance, e);
         }
     }
   }
   
   protected void callPostConstruct(T instance)
   {
      AnnotatedMethod<Object> postConstruct = getPostConstruct();
      if (postConstruct!=null)
      {
         try
         {
            postConstruct.invoke(instance);
         }
         catch (Exception e) 
         {
            throw new RuntimeException("Unable to invoke " + postConstruct + " on " + instance, e);
         }
      }
   }

   protected void callInitializers(T instance)
   {
      for (InjectableMethod<Object> initializer : getInitializerMethods())
      {
         initializer.invoke(getManager(), instance);
      }
   }
   
   protected void injectEjbAndCommonFields()
   {
      // TODO
   }
   
   protected void injectBoundFields(T instance)
   {
      for (InjectableField<?> injectableField : getInjectableFields())
      {
         injectableField.inject(instance, getManager());
      }
   }
   
   @Override
   protected void init()
   {
      super.init();
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
      for (AnnotatedParameter<Object> parameter : constructor.getParameters())
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
      log.trace("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + getType());
      if (initializerAnnotatedConstructors.size() > 1)
      {
         if (initializerAnnotatedConstructors.size() > 1)
         {
            throw new DefinitionException("Cannot have more than one constructor annotated with @Initializer for " + getType());
         }
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         this.constructor = initializerAnnotatedConstructors.iterator().next();
         log.trace("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the bean constructor for " + getType());
         return;
      }
         
      if (getAnnotatedItem().getConstructor(NO_ARGUMENTS) != null)
      {
         
         this.constructor =getAnnotatedItem().getConstructor(NO_ARGUMENTS);
         log.trace("Exactly one constructor (" + constructor +") defined, using it as the bean constructor for " + getType());
         return;
      }
      
      throw new DefinitionException("Cannot determine constructor to use for " + getType());
   }
   
   protected void initPostConstruct()
   {
      Set<AnnotatedMethod<Object>> postConstructMethods = getAnnotatedItem().getAnnotatedMethods(PostConstruct.class);
      log.trace("Found " + postConstructMethods + " constructors annotated with @Initializer for " + getType());
      if (postConstructMethods.size() > 1)
      {
         //TODO: actually this is wrong, in EJB you can have @PostConstruct methods on the superclass,
         //      though the Web Beans spec is silent on the issue
         throw new DefinitionException("Cannot have more than one post construct method annotated with @Initializer for " + getType());
      }
      else if (postConstructMethods.size() == 1)
      {
         this.postConstruct = postConstructMethods.iterator().next();
         log.trace("Exactly one post construct method (" + postConstruct +") for " + getType());
        return;
      }
   }

   protected void initPreDestroy()
   {
      Set<AnnotatedMethod<Object>> preDestroyMethods = getAnnotatedItem().getAnnotatedMethods(PreDestroy.class);
      log.trace("Found " + preDestroyMethods + " constructors annotated with @Initializer for " + getType());
      if (preDestroyMethods.size() > 1)
      {
         //TODO: actually this is wrong, in EJB you can have @PreDestroy methods on the superclass,
         //      though the Web Beans spec is silent on the issue
         throw new DefinitionException("Cannot have more than one pre destroy method annotated with @Initializer for " + getType());
      }
      else if (preDestroyMethods.size() == 1)
      {
         this.preDestroy = preDestroyMethods.iterator().next();
         log.trace("Exactly one post construct method (" + preDestroy +") for " + getType());
        return;
      }
   }


   public AnnotatedConstructor<T> getConstructor()
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

   protected AbstractBean<? extends T, Class<T>> getSpecializedType()
   {
      //TODO: lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if ( superclass!=null )
      {
         return new SimpleBean(superclass, getManager());
      }
      else
      {
         throw new RuntimeException();
      }
   }
   
   public AnnotatedMethod<Object> getPostConstruct() 
   {
      return postConstruct;
   }
   
   public AnnotatedMethod<Object> getPreDestroy() 
   {
      return preDestroy;
   }
   
}
