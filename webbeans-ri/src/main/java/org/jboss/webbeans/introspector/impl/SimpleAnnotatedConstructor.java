package org.jboss.webbeans.introspector.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import org.jboss.webbeans.introspector.AnnotatedConstructor;

public class SimpleAnnotatedConstructor<T> extends AbstractAnnotatedMember<T, Constructor<T>> implements AnnotatedConstructor<T>
{

   private static final Type[] actualTypeArguments = new Type[0];
   
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

   public Constructor<T> getDelegate()
   {
      return constructor;
   }
   
   public Class<T> getType()
   {
      return constructor.getDeclaringClass();
   }
   
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

}
