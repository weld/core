package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class Reflections
{
   
   public static Class<?> classForName(String name) throws ClassNotFoundException
   {
      try 
      {
         return Thread.currentThread().getContextClassLoader().loadClass(name);
      }
      catch (Exception e)
      {
         return Class.forName(name);
      }
   }

   public static boolean isFinal(Class<?> clazz)
   {
      return Modifier.isFinal(clazz.getModifiers());
   }
   
   public static boolean isFinal(Method method)
   {
      return Modifier.isFinal(method.getModifiers());
   }
   
   public static boolean isAbstract(Class<?> clazz)
   {
      return Modifier.isAbstract(clazz.getModifiers());
   }
   
   public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes)
   {
      try
      {
         return clazz.getConstructor(parameterTypes);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error accessing constructor (with parameters " + parameterTypes + ") of " + clazz, e);
      }
   }
   
   public static List<Method> getMethods(Class<?> clazz, Class<? extends Annotation> annotationType) 
   {
      List<Method> methods = new ArrayList<Method>();
      for (Method method : clazz.getMethods())
      {
         if (method.isAnnotationPresent(annotationType))
         {
            methods.add(method);
         }
      }
      return methods;
   }
   
   public static <T> List<Constructor<T>> getConstructors(Class<? extends T> clazz, Class<? extends Annotation> annotationType) 
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<T> constructor : clazz.getConstructors())
      {
         if (constructor.isAnnotationPresent(annotationType))
         {
            constructors.add(constructor);
         }
      }
      return constructors;
   }
   
   public static <T> List<Constructor<T>> getConstructorsForAnnotatedParameter(Class<? extends T> clazz, Class<? extends Annotation> parameterAnnotationType) 
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<T> constructor : clazz.getConstructors())
      {
         for (Annotation[] annotations : constructor.getParameterAnnotations())
         {
            for (Annotation annotation : annotations)
            {
               if (annotation.annotationType().equals(parameterAnnotationType))
               {
                  constructors.add(constructor);
               }
            }
         }
      }
      return constructors;
   }
   
   public static <T> List<Constructor<T>> getConstructorsForMetaAnnotatedParameter(Class<? extends T> clazz, Class<? extends Annotation> metaAnnotationType) 
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<T> constructor : clazz.getConstructors())
      {
         for (Annotation[] annotations : constructor.getParameterAnnotations())
         {
            for (Annotation annotation : annotations)
            {
               if (annotation.annotationType().isAnnotationPresent(metaAnnotationType))
               {
                  constructors.add(constructor);
               }
            }
         }
      }
      return constructors;
   }
}
