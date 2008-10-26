package org.jboss.webbeans.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


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
   
   public static String getPropertyName(Method method)
   {
      String methodName = method.getName();
      if ( methodName.matches("^(get).*") && method.getParameterTypes().length==0 )
      {
         return Introspector.decapitalize( methodName.substring(3) );
      }
      else if (methodName.matches("^(is).*") && method.getParameterTypes().length==0 )
      {
         return Introspector.decapitalize( methodName.substring(2) );
      }
      else
      {
         return null;
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
   
   public static boolean isTypeOrAnyMethodFinal(Class<?> type)
   {
      if (isFinal(type))
      {
         return true;
      }
      for (Method method : type.getDeclaredMethods())
      {
         if (isFinal(method))
         {
            return true;
         }
      }
      return false;
   }
   
   public static boolean isPrimitive(Class<?> type)
   {
      return type.isPrimitive();
   }
   
   public static boolean isAbstract(Class<?> clazz)
   {
      return Modifier.isAbstract(clazz.getModifiers());
   }
   
   public static boolean isStaticInnerClass(Class<?> clazz)
   {
      return clazz.isMemberClass() && Modifier.isStatic(clazz.getModifiers());
   }
   
   public static boolean isNonStaticInnerClass(Class<?> clazz)
   {
      return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
   }
   
   public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes)
   {
      try
      {
         return clazz.getConstructor(parameterTypes);
      }
      catch (NoSuchMethodException e) 
      {
         return null;
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
   
   @SuppressWarnings("unchecked")
   public static <T> List<Constructor<T>> getAnnotatedConstructors(Class<? extends T> clazz, Class<? extends Annotation> annotationType) 
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         if (constructor.isAnnotationPresent(annotationType))
         {
            constructors.add((Constructor<T>) constructor);
         }
      }
      return constructors;
   }
   
   @SuppressWarnings("unchecked")
   public static <T> List<Constructor<T>> getConstructorsForAnnotatedParameter(Class<? extends T> clazz, Class<? extends Annotation> parameterAnnotationType) 
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         for (Annotation[] annotations : constructor.getParameterAnnotations())
         {
            for (Annotation annotation : annotations)
            {
               if (annotation.annotationType().equals(parameterAnnotationType))
               {
                  constructors.add((Constructor<T>) constructor);
               }
            }
         }
      }
      return constructors;
   }
   
   @SuppressWarnings("unchecked")
   public static <T> List<Constructor<T>> getConstructorsForMetaAnnotatedParameter(Class<? extends T> clazz, Class<? extends Annotation> metaAnnotationType) 
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         for (Annotation[] annotations : constructor.getParameterAnnotations())
         {
            for (Annotation annotation : annotations)
            {
               if (annotation.annotationType().isAnnotationPresent(metaAnnotationType))
               {
                  constructors.add((Constructor<T>) constructor);
               }
            }
         }
      }
      return constructors;
   }

   public static boolean annotationTypeSetMatches(Set<Class<? extends Annotation>> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Class<? extends Annotation> annotation : annotations)
      {
         if (annotationTypeList.contains(annotation))
         {
            annotationTypeList.remove(annotation);
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
   }

   public static boolean annotationSetMatches(Set<Annotation> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Annotation annotation : annotations)
      {
         if (annotationTypeList.contains(annotation.annotationType()))
         {
            annotationTypeList.remove(annotation.annotationType());
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
   }
   
   public static Type[] getActualTypeArguements(Class<?> clazz)
   {
      if (clazz.getGenericSuperclass() instanceof ParameterizedType)
      {
         return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments(); 
      }
      else
      {
         return new Type[0];
      }
   }

   public static boolean isArrayType(Class<?> rawType)
   {
      return rawType.isArray();
   }

   public static boolean isParameterizedType(Class<?> type)
   {
      return type.getTypeParameters().length > 0;
   }
}
