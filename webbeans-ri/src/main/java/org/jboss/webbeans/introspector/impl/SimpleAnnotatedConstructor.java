package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class SimpleAnnotatedConstructor<T> extends AbstractAnnotatedMember<T, Constructor<T>> implements AnnotatedConstructor<T>
{

   private static final Type[] actualTypeArguments = new Type[0];
   
   private Constructor<T> constructor;
   
   private Set<AnnotatedParameter<Object>> parameters;
   private Map<Class<? extends Annotation>, Set<AnnotatedParameter<Object>>> annotatedParameters;
   
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
      for (int i = 0; i < constructor.getParameterTypes().length; i++)
      {
         if (constructor.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = constructor.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new SimpleAnnotatedParameter<Object>(constructor.getParameterAnnotations()[i], clazz);
            parameters.add(parameter);
         }
         else
         {
            Class<? extends Object> clazz = constructor.getParameterTypes()[i];
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
      
      if (super.equals(other) && other instanceof AnnotatedConstructor)
      {
         AnnotatedConstructor<?> that = (AnnotatedConstructor<?>) other;
         return this.getDelegate().equals(that.getDelegate());
      }
      return false;
   }
   
   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

}
