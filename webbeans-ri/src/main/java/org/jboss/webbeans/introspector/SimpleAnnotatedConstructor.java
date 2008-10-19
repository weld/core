package org.jboss.webbeans.introspector;

import java.lang.reflect.Constructor;

public class SimpleAnnotatedConstructor<T> extends AbstractAnnotatedItem<T, Constructor<T>> implements AnnotatedConstructor<T>
{
   
   private Constructor<T> constructor;
   
   public SimpleAnnotatedConstructor(Constructor<T> constructor)
   {
      super(buildAnnotationMap(constructor));
      this.constructor = constructor;
   }

   public Constructor<T> getAnnotatedConstructor()
   {
      return constructor;
   }
   
   @Override
   public String toString()
   {
      return constructor + " " + getAnnotatedConstructor().toString();
   }

   public Constructor<T> getDelegate()
   {
      return constructor;
   }
   
   @SuppressWarnings("unchecked")
   public Class<? extends T> getType()
   {
      return constructor.getDeclaringClass();
   }

}
