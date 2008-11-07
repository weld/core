package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class SimpleAnnotatedConstructor<T> extends AbstractAnnotatedMember<T, Constructor<T>> implements AnnotatedConstructor<T>
{

   private static final Type[] actualTypeArguments = new Type[0];
   
   private Constructor<T> constructor;
   
   private List<AnnotatedParameter<Object>> parameters;
   private Map<Class<? extends Annotation>, List<AnnotatedParameter<Object>>> annotatedParameters;
   
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
   
   public List<AnnotatedParameter<Object>> getParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      return parameters;
   }
   
   private void initParameters()
   {
      this.parameters = new ArrayList<AnnotatedParameter<Object>>();
      for (int i = 0; i < constructor.getParameterTypes().length; i++)
      {
         if (constructor.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = constructor.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new SimpleAnnotatedParameter<Object>(constructor.getParameterAnnotations()[i], (Class<Object>) clazz);
            parameters.add(parameter);
         }
         else
         {
            Class<? extends Object> clazz = constructor.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new SimpleAnnotatedParameter<Object>(new Annotation[0], (Class<Object>) clazz);
            parameters.add(parameter);
         }
      }
   }
   
   public List<AnnotatedParameter<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
       
      if (!annotatedParameters.containsKey(annotationType))
      {
         return new ArrayList<AnnotatedParameter<Object>>();
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
      annotatedParameters = new HashMap<Class<? extends Annotation>, List<AnnotatedParameter<Object>>>();
      for (AnnotatedParameter<Object> parameter : parameters)
      {
         for (Annotation annotation : parameter.getAnnotations())
         {
            if (!annotatedParameters.containsKey(annotation))
            {
               annotatedParameters.put(annotation.annotationType(), new ArrayList<AnnotatedParameter<Object>>());
            }
            annotatedParameters.get(annotation.annotationType()).add(parameter);
         }
      }
   }

   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
      if (!annotatedParameters.containsKey(annotationType))
      {
         return new ArrayList<AnnotatedParameter<Object>>();
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
