package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class SimpleAnnotatedMethod<T> extends AbstractAnnotatedMember<T, Method> implements AnnotatedMethod<T>
{
   
   private static final Type[] actualTypeArgements = new Type[0];
   
   private Method method;
   
   private Set<AnnotatedParameter<Object>> parameters;
   private Map<Class<? extends Annotation>, Set<AnnotatedParameter<Object>>> annotatedParameters;
   
   public SimpleAnnotatedMethod(Method method)
   {
      super(buildAnnotationMap(method));
      this.method = method;
   }

   public Method getAnnotatedMethod()
   {
      return method;
   }

   public Method getDelegate()
   {
      return method;
   }
   
   public Class<T> getType()
   {
      return (Class<T>) method.getReturnType();
   }
   
   public Type[] getActualTypeArguments()
   {
      return actualTypeArgements;
   }
   
   public Set<AnnotatedParameter<Object>> getParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      return parameters;
   }
   
   private void initParameters()
   {
      this.parameters = new HashSet<AnnotatedParameter<Object>>();
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         if (method.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new SimpleAnnotatedParameter<Object>(method.getParameterAnnotations()[i], clazz);
            parameters.add(parameter);
         }
         else
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new SimpleAnnotatedParameter<Object>(new Annotation[0], clazz);
            parameters.add(parameter);
         }
      }
   }
   
   public Set<AnnotatedParameter<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
       
      if (!annotatedParameters.containsKey(annotationType))
      {
         return new HashSet<AnnotatedParameter<Object>>();
      }
      else
      {
         return annotatedParameters.get(annotationType);
      }
   }

   private void initAnnotatedParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      annotatedParameters = new HashMap<Class<? extends Annotation>, Set<AnnotatedParameter<Object>>>();
      for (AnnotatedParameter<Object> parameter : parameters)
      {
         for (Annotation annotation : parameter.getAnnotations())
         {
            if (!annotatedParameters.containsKey(annotation))
            {
               annotatedParameters.put(annotation.annotationType(), new HashSet<AnnotatedParameter<Object>>());
            }
            annotatedParameters.get(annotation.annotationType()).add(parameter);
         }
      }
   }

   public Set<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
      if (!annotatedParameters.containsKey(annotationType))
      {
         return new HashSet<AnnotatedParameter<Object>>();
      }
      return annotatedParameters.get(annotationType);
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AnnotatedMethod)
      {
         AnnotatedMethod<?> that = (AnnotatedMethod<?>) other;
         return this.getDelegate().equals(that.getDelegate());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

}
