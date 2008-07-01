package org.jboss.webbeans;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.Container;
import javax.webbeans.Initializer;

import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;

public class ConstructorMetaModel<T>
{
   
public static final String LOGGER_NAME = "componentConstructor";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);

   private Constructor<T> constructor;
   
   private List<AbstractInjectedThingMetaModel> injectedParameters;

   @SuppressWarnings("unchecked")
   public ConstructorMetaModel(Class<? extends T> type)
   {
      this.injectedParameters = new ArrayList<AbstractInjectedThingMetaModel>();
      if (type.getConstructors().length == 1)
      {
         this.constructor = type.getConstructors()[0];
         log.finest("Exactly one constructor (" + constructor +") defined, using it as the component constructor for " + type);
      }
      else if (type.getConstructors().length > 1)
      {
         
         List<Constructor<T>> initializerAnnotatedConstructors = Reflections.getConstructors(type, Initializer.class);
         List<Constructor<T>> bindingTypeAnnotatedConstructors = Reflections.getConstructorsForMetaAnnotatedParameter(type, BindingType.class);
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
            this.constructor = initializerAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the component constructor for " + type);
         }
         else if (bindingTypeAnnotatedConstructors.size() == 1)
         {
            this.constructor = bindingTypeAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") with parameters annotated with binding types defined, using it as the component constructor for " + type);
         }
      }
      else if (type.getConstructors().length == 0)
      {      
         this.constructor = (Constructor<T>) Reflections.getConstructor(type);
         log.finest("No constructor defined, using implicit no arguement constructor");
      }
      
      for (int i = 0; i < constructor.getParameterTypes().length; i++)
      {
         if (constructor.getParameterAnnotations()[i].length > 0)
         {
            InjectedParameterMetaModel parameter = new InjectedParameterMetaModel(constructor.getParameterAnnotations()[i], constructor.getParameterTypes()[i]);
            injectedParameters.add(i, parameter);
         }
         else
         {
            InjectedParameterMetaModel parameter = new InjectedParameterMetaModel(constructor.getParameterTypes()[i]);
            injectedParameters.add(i, parameter);
         }
      }
      log.finest("Initialized metadata for " + constructor + " with injectable parameters " + injectedParameters);
      if (this.constructor == null)
      {
         throw new RuntimeException("Cannot determine constructor to use");
      }
   }
   
   public Constructor<T> getConstructor()
   {
      return constructor;
   }
   
   public List<AbstractInjectedThingMetaModel> getInjectedAttributes()
   {
      return injectedParameters;
   }
   
   public T newInstance(Container container)
   {
      Object[] parameters = new Object[injectedParameters.size()];
      log.finest("Creating new instance of " + constructor.getDeclaringClass() + " with injected parameters " + injectedParameters);
      for (int i = 0; i < injectedParameters.size(); i++)
      {
         parameters[i] = container.getInstanceByType(injectedParameters.get(i).getType(), injectedParameters.get(0).getBindingTypes());
      }
      try
      {
         return constructor.newInstance(parameters);
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Error instantiating " + constructor.getDeclaringClass(), e);
      }
   }
   
}
