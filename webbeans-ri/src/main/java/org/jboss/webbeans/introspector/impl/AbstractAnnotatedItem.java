package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.proxy.ClientProxy;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Types;

public abstract class AbstractAnnotatedItem<T, S> implements AnnotatedItem<T, S>
{

   private static final Annotation[] DEFAULT_BINDING_ARRAY = {new CurrentAnnotationLiteral()};
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   
   private Map<Class<? extends Annotation>, Annotation> annotationMap;
   private Map<Class<? extends Annotation>, Set<Annotation>> metaAnnotationMap;
   private Set<Annotation> annotationSet;
   private Annotation[] annotationArray;
   
   public AbstractAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
   }
   
   protected static Map<Class<? extends Annotation>, Annotation> buildAnnotationMap(AnnotatedElement element)
   {
      return buildAnnotationMap(element.getAnnotations());
   }
   
   protected static Map<Class<? extends Annotation>, Annotation> buildAnnotationMap(Annotation[] annotations)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      for (Annotation annotation : annotations)
      {
         annotationMap.put(annotation.annotationType(), annotation);
      }
      return annotationMap;
   }

   protected static Set<Annotation> populateAnnotationSet(Set<Annotation> annotationSet, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      for (Entry<Class<? extends Annotation>, Annotation> entry : annotationMap.entrySet())
      {
         annotationSet.add(entry.getValue());
      }
      return annotationSet;
   }
   
   protected static Object[] getParameterValues(List<AnnotatedParameter<Object>> parameters, ManagerImpl manager)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<AnnotatedParameter<Object>> iterator = parameters.iterator();   
      for (int i = 0; i < parameterValues.length; i++)
      {
         parameterValues[i] = iterator.next().getValue(manager);
      }
      return parameterValues;
   }

   public <A extends Annotation> A getAnnotation(Class<? extends A> annotationType)
   {
      return (A) annotationMap.get(annotationType);
   }

   public Set<Annotation> getAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      if (metaAnnotationMap == null)
      {
         metaAnnotationMap = new HashMap<Class<? extends Annotation>, Set<Annotation>>();
      }
      metaAnnotationMap = populateMetaAnnotationMap(metaAnnotationType, metaAnnotationMap, annotationMap);
      return metaAnnotationMap.get(metaAnnotationType);
   }
   
   public Annotation[] getAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      if (annotationArray == null)
      {
         annotationArray = new Annotation[0];
         annotationArray = getAnnotations(metaAnnotationType).toArray(annotationArray);
      }
      return annotationArray;
   }

   public Set<Annotation> getAnnotations()
   {
      if (annotationSet == null)
      {
         annotationSet = populateAnnotationSet(new HashSet<Annotation>(), annotationMap);
      }
      return annotationSet;
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotatedType)
   {
      return annotationMap.containsKey(annotatedType);
   }

   protected static <A extends Annotation> Map<Class<? extends Annotation>, Set<Annotation>> populateMetaAnnotationMap(
         Class<A> metaAnnotationType, Map<Class<? extends Annotation>, 
         Set<Annotation>> metaAnnotationMap, 
         Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      if (!metaAnnotationMap.containsKey(metaAnnotationType))
      {
         Set<Annotation> s = new HashSet<Annotation>();
         for (Entry<Class<? extends Annotation>, Annotation> entry : annotationMap.entrySet())
         {
            if (entry.getValue().annotationType().isAnnotationPresent(metaAnnotationType))
            {
               s.add(entry.getValue());
            }
         }
         metaAnnotationMap.put(metaAnnotationType, s);
      }
      return metaAnnotationMap;
   }

   protected Map<Class<? extends Annotation>, Annotation> getAnnotationMap()
   {
      return annotationMap;
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AnnotatedItem)
      {
         AnnotatedItem<?, ?> that = (AnnotatedItem<?, ?>) other;
         return this.getAnnotations().equals(that.getAnnotations()) && this.getType().equals(that.getType());
      }
      return false;
   }
   
   public boolean isAssignableFrom(AnnotatedItem<?, ?> that)
   {
      return isAssignableFrom(that.getType(), that.getActualTypeArguments());
   }
   
   public boolean isAssignableFrom(Set<Class<?>> types)
   {
      for (Class<?> type : types)
      {
         if (isAssignableFrom(type, Reflections.getActualTypeArguments(type)))
         {
            return true;
         }
      }
      return false;
   }
   
   private boolean isAssignableFrom(Class<?> type, Type[] actualTypeArguments)
   {
      return Types.boxedType(getType()).isAssignableFrom(Types.boxedType(type)) && Arrays.equals(getActualTypeArguments(), actualTypeArguments);
   }
   
   @Override
   public int hashCode()
   {
      return getType().hashCode();
   }
   
   @Override
   public String toString()
   {
      String string = getType().toString();
      if (getActualTypeArguments().length > 0)
      {
         string += "<";
         for (int i = 0; i < getActualTypeArguments().length; i++)
         {
            string += getActualTypeArguments()[i].toString();
            if (i < getActualTypeArguments().length - 1)
            {
               string += ",";
            }
         }
         string += ">";
      }
      string += getAnnotations();
      return string;
   }
   
   public Set<Annotation> getBindingTypes()
   {
      if (getAnnotations(BindingType.class).size() > 0)
      {
         return getAnnotations(BindingType.class);
      }
      else
      {
         return DEFAULT_BINDING;
      }
   }
   
   public Annotation[] getBindingTypesAsArray()
   {
      if (getAnnotationsAsArray(BindingType.class).length > 0)
      {
         return getAnnotationsAsArray(BindingType.class);
      }
      else
      {
         return DEFAULT_BINDING_ARRAY;
      }
   }
   
   public boolean isProxyable()
   {
      return ClientProxy.isProxyable(getType());
   }

}