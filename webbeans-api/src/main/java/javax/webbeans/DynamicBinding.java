package javax.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class DynamicBinding<T extends Annotation> implements Annotation
{

   private Class<T> annotationType;
   
   @SuppressWarnings("unchecked")
   public DynamicBinding()
   {
      annotationType = getAnnotationType(getClass());
      if (annotationType == null)
      {
         throw new RuntimeException("Unable to determine type of dynamic binding for " + getClass());
      }
   }
   
   private static <T> Class<T> getAnnotationType(Class<?> clazz)
   {
      Type type = clazz.getGenericSuperclass();
      Class<T> annotationType = null;
      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         if (parameterizedType.getActualTypeArguments().length == 1)
         {
            annotationType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
         }
      }
      if (annotationType == null && clazz != Object.class)
      {
         return getAnnotationType(clazz.getSuperclass());
      }
      else
      {
         return annotationType;
      }
   }
   
   public Class<? extends Annotation> annotationType()
   {
      return annotationType;
   }
   
   @Override
   public String toString()
   {
      // TODO Make this closer to the spec for Annotation
      String annotationName = "@" + annotationType.getName();
      return annotationName;
   }
   
}
