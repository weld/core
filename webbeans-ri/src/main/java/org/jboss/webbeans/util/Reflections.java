package org.jboss.webbeans.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.ExecutionException;

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
      if (methodName.matches("^(get).*") && method.getParameterTypes().length == 0)
      {
         return Introspector.decapitalize(methodName.substring(3));
      }
      else if (methodName.matches("^(is).*") && method.getParameterTypes().length == 0)
      {
         return Introspector.decapitalize(methodName.substring(2));
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

   public static boolean isFinal(Member member)
   {
      return Modifier.isFinal(member.getModifiers());
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

   public static boolean isStatic(Class<?> type)
   {
      return Modifier.isStatic(type.getModifiers());
   }

   public static boolean isStatic(Member member)
   {
      return Modifier.isStatic(member.getModifiers());
   }

   public static boolean isAbstract(Class<?> clazz)
   {
      return Modifier.isAbstract(clazz.getModifiers());
   }

   public static boolean isStaticInnerClass(Class<?> clazz)
   {
      return clazz.isMemberClass() && isStatic(clazz);
   }

   public static boolean isNonStaticInnerClass(Class<?> clazz)
   {
      return clazz.isMemberClass() && !isStatic(clazz);
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

   public static Type[] getActualTypeArguments(Class<?> clazz)
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

   public static Object invokeAndWrap(Method method, Object instance, Object... parameters)
   {
      try
      {
         return method.invoke(instance, parameters);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error invoking method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error invoking method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (InvocationTargetException e)
      {
         throw new ExecutionException("Error invoking method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
   }

   public static void setAndWrap(Field field, Object target, Object value)
   {
      try
      {
         field.set(target, value);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error setting field " + field.getName() + " on " + field.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error setting field " + field.getName() + " on " + field.getDeclaringClass(), e);
      }
   }

   public static Method lookupMethod(Method method, Object instance)
   {
      for (Class<? extends Object> clazz = instance.getClass(); clazz != Object.class; clazz = clazz.getSuperclass())
      {
         try
         {
            Method targetMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (!targetMethod.isAccessible())
            {
               targetMethod.setAccessible(true);
            }
            return targetMethod;
         }
         catch (NoSuchMethodException nsme)
         {
            // Expected, nothing to see here.
         }
      }
      throw new IllegalArgumentException("Method " + method.getName() + " not implemented by instance");
   }

   public static boolean isProxy(Object instance)
   {
      return instance.getClass().getName().indexOf("_$$_javassist_") > 0;
   }

   /**
    * Gets the type hierarchy for a class
    * 
    * A recursive function that adds the class to the set of type and then calls
    * itself with the suprerclass as paramater until the top of the hierarchy is
    * reached. For each steps, adds all interfaces of the class to the set.
    * Since the data structure is a set, duplications are eliminated
    * 
    * @param clazz The class to examine
    * @return The set of classes and interfaces in the hierarchy
    */
   public static Set<Class<?>> getTypeHierachy(Class<?> clazz)
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      if (clazz != null)
      {
         classes.add(clazz);
         classes.addAll(getTypeHierachy(clazz.getSuperclass()));
         for (Class<?> c : clazz.getInterfaces())
         {
            classes.addAll(getTypeHierachy(c));
         }
      }
      return classes;
   }

}
