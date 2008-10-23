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
   
   public List<InjectableParameter<?>> getParameters()
   {
      return parameters;
   }

   @SuppressWarnings("unchecked")
   protected static <T> List<InjectableParameter<?>> initParameters(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      List<InjectableParameter<?>> injectedParameters = new ArrayList<InjectableParameter<?>>();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         if (parameterAnnotations[i].length > 0)
         {
            InjectableParameter<Object> parameter = new InjectableParameter(parameterAnnotations[i], parameterTypes[i]);
            injectedParameters.add(i, parameter);
         }
         else
         {
            InjectableParameter<Object> parameter = new InjectableParameter(parameterTypes[i]);
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
