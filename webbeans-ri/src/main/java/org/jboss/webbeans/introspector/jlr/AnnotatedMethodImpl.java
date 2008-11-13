package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.Reflections;

public class AnnotatedMethodImpl<T> extends AbstractAnnotatedMember<T, Method> implements AnnotatedMethod<T>
{
   
   private Type[] actualTypeArgements = new Type[0];
   
   private Method method;
   
   private List<AnnotatedParameter<Object>> parameters;
   private Map<Class<? extends Annotation>, List<AnnotatedParameter<Object>>> annotatedParameters;

   private String propertyName;

   private AnnotatedType<?> declaringClass;
   
   public AnnotatedMethodImpl(Method method, AnnotatedType<?> declaringClass)
   {
      super(buildAnnotationMap(method));
      this.method = method;
      this.declaringClass = declaringClass;
      if (method.getGenericReturnType() instanceof ParameterizedType)
      {
         actualTypeArgements = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
      }
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
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         if (method.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(method.getParameterAnnotations()[i], (Class<Object>) clazz);
            parameters.add(parameter);
         }
         else
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(new Annotation[0], (Class<Object>) clazz);
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

   public T invoke(ManagerImpl manager, Object instance)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, getParameterValues(parameters, manager));
   }
   
   public T invoke(Object instance, Object... parameters)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, parameters);
   }
   
   public String getPropertyName()
   {
      if (propertyName == null)
      {
         propertyName = Reflections.getPropertyName(getDelegate());
         if (propertyName == null)
         {
            propertyName = getName();
         }
      }
      return propertyName;
   }

   public AnnotatedType<?> getDeclaringClass()
   {
      return declaringClass;
   }

}
