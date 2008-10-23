package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;

public abstract class Unit<T, S>
{

   private List<InjectableParameter<?>> parameters;
   
   public Unit(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      parameters = initParameters(parameterTypes, parameterAnnotations);
   }
   
   public List<InjectableParameter<? extends Object>> getParameters()
   {
      return parameters;
   }

   protected static <T> List<InjectableParameter<?>> initParameters(Class<? extends Object>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      List<InjectableParameter<?>> injectedParameters = new ArrayList<InjectableParameter<?>>();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         if (parameterAnnotations[i].length > 0)
         {
            Class<? extends Object> clazz = parameterTypes[i];
            InjectableParameter<? extends Object> parameter = new InjectableParameter<Object>(parameterAnnotations[i], clazz);
            injectedParameters.add(i, parameter);
         }
         else
         {
            InjectableParameter<?> parameter = new InjectableParameter<Object>(parameterTypes[i]);
            injectedParameters.add(i, parameter);
         }
      }
      return injectedParameters;
   }
   
   public Object[] getParameterValues(ManagerImpl manager)
   {
      Object[] parameterValues = new Object[parameters.size()];
      for (int i = 0; i < parameterValues.length; i++)
      {
         parameterValues[i] = parameters.get(i).getValue(manager);
      }
      return parameterValues;
   }

   public abstract AnnotatedItem<T, S> getAnnotatedItem();
   
}
