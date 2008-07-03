package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Container;

public abstract class Unit<T>
{

   private List<Element<Object>> parameters;
   
   public Unit(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      parameters = initParameters(parameterTypes, parameterAnnotations);
   }
   
   public List<Element<Object>> getParameters()
   {
      return parameters;
   }

   @SuppressWarnings("unchecked")
   protected static List<Element<Object>> initParameters(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      List<Element<Object>> injectedParameters = new ArrayList<Element<Object>>();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         if (parameterAnnotations[i].length > 0)
         {
            Parameter<Object> parameter = new Parameter(parameterAnnotations[i], parameterTypes[i]);
            injectedParameters.add(i, parameter);
         }
         else
         {
            Parameter<Object> parameter = new Parameter(parameterTypes[i]);
            injectedParameters.add(i, parameter);
         }
      }
      return injectedParameters;
   }
   
   public Object[] getParameterValues(Container container)
   {
      Object[] parameterValues = new Object[parameters.size()];
      for (int i = 0; i < parameterValues.length; i++)
      {
         parameterValues[i] = parameters.get(i).getValue(container);
      }
      return parameterValues;
   }

}
