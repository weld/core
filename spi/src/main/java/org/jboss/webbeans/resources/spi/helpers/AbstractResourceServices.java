package org.jboss.webbeans.resources.spi.helpers;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.inject.ExecutionException;
import javax.inject.manager.InjectionPoint;
import javax.naming.Context;
import javax.naming.NamingException;

public abstract class AbstractResourceServices
{
   
   private static final String RESOURCE_LOOKUP_PREFIX = "java:/comp/env";
   
   /* (non-Javadoc)
    * @see org.jboss.webbeans.resources.spi.helpers.ResourceServices#resolveResource(javax.inject.manager.InjectionPoint)
    */
   public Object resolveResource(InjectionPoint injectionPoint)
   {
      if (!injectionPoint.isAnnotationPresent(Resource.class))
      {
         throw new IllegalArgumentException("No @Resource annotation found on injection point " + injectionPoint);
      }
      if (injectionPoint.getMember() instanceof Method && ((Method) injectionPoint.getMember()).getParameterTypes().length != 1)
      {
         throw new IllegalArgumentException("Injection point represents a method which doesn't follow JavaBean conventions (must have exactly one parameter) " + injectionPoint);
      }
      String name = getResourceName(injectionPoint);
      try
      {
         return getContext().lookup(name);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Error looking up " + name + " in JNDI", e);
      }
   }
   
   public Object resolveResource(String jndiName, String mappedName)
   {
      String name = getResourceName(jndiName, mappedName);
      try
      {
         return getContext().lookup(name);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Error looking up " + name + " in JNDI", e);
      }
   }
   
   protected String getResourceName(String jndiName, String mappedName)
   {
      if (mappedName != null)
      {
         return mappedName;
      }
      else if (jndiName != null)
      {
         return jndiName;
      }
      else
      {
         throw new IllegalArgumentException("Both jndiName and mappedName are null");
      }
   }
   
   protected abstract Context getContext();
   
   protected String getResourceName(InjectionPoint injectionPoint)
   {
      Resource resource = injectionPoint.getAnnotation(Resource.class);
      String mappedName = resource.mappedName();
      if (!mappedName.equals(""))
      {
         return mappedName;
      }
      String name = resource.name();
      if (!name.equals(""))
      {
         return RESOURCE_LOOKUP_PREFIX + "/" + name;
      }
      String propertyName;
      if (injectionPoint.getMember() instanceof Field)
      {
         propertyName = injectionPoint.getMember().getName();
      }
      else if (injectionPoint.getMember() instanceof Method)
      {
         propertyName = getPropertyName((Method) injectionPoint.getMember());
         if (propertyName == null)
         {
            throw new IllegalArgumentException("Injection point represents a method which doesn't follow JavaBean conventions (unable to determine property name) " + injectionPoint);
         }
      }
      else
      {
         throw new AssertionError("Unable to inject into " + injectionPoint);
      }
      String className = injectionPoint.getMember().getDeclaringClass().getName();
      return RESOURCE_LOOKUP_PREFIX + "/" + className + "/" + propertyName;
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
   
}
