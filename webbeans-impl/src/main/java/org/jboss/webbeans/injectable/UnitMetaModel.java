package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Container;

public abstract class UnitMetaModel<T>
{

   private List<ElementMetaModel<Object>> parameters;
   
   public UnitMetaModel(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      parameters = initParameters(parameterTypes, parameterAnnotations);
   }
   
   public List<ElementMetaModel<Object>> getParameters()
   {
      return parameters;
   }

   @SuppressWarnings("unchecked")
   protected static List<ElementMetaModel<Object>> initParameters(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
   {
      List<ElementMetaModel<Object>> injectedParameters = new ArrayList<ElementMetaModel<Object>>();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         if (parameterAnnotations[i].length > 0)
         {
            ParameterMetaModel<Object> parameter = new ParameterMetaModel(parameterAnnotations[i], parameterTypes[i]);
            injectedParameters.add(i, parameter);
         }
         else
         {
            ParameterMetaModel<Object> parameter = new ParameterMetaModel(parameterTypes[i]);
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
