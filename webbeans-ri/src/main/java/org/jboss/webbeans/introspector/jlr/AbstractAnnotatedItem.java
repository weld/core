package org.jboss.webbeans.introspector.jlr;

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
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Types;

import com.google.common.collect.ForwardingMap;

public abstract class AbstractAnnotatedItem<T, S> implements AnnotatedItem<T, S>
{

   public static class AnnotationMap extends ForwardingMap<Class<? extends Annotation>, Annotation>
   {
      private Map<Class<? extends Annotation>, Annotation> delegate;

      public AnnotationMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Annotation>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Annotation> delegate()
      {
         return delegate;
      }

   }

   public static class MetaAnnotationMap extends ForwardingMap<Class<? extends Annotation>, Set<Annotation>>
   {
      private Map<Class<? extends Annotation>, Set<Annotation>> delegate;

      public MetaAnnotationMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<Annotation>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<Annotation>> delegate()
      {
         return delegate;
      }

   }

   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new CurrentAnnotationLiteral() };
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));

   private AnnotationMap annotationMap;
   private MetaAnnotationMap metaAnnotationMap;
   private Set<Annotation> annotationSet;
   private Annotation[] annotationArray;

   public AbstractAnnotatedItem(AnnotationMap annotationMap)
   {
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
   }

   protected static AnnotationMap buildAnnotationMap(AnnotatedElement element)
   {
      return buildAnnotationMap(element.getAnnotations());
   }

   protected static AnnotationMap buildAnnotationMap(Annotation[] annotations)
   {
      AnnotationMap annotationMap = new AnnotationMap();
      for (Annotation annotation : annotations)
      {
         annotationMap.put(annotation.annotationType(), annotation);
      }
      return annotationMap;
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

   @SuppressWarnings("unchecked")
   public <A extends Annotation> A getAnnotation(Class<? extends A> annotationType)
   {
      return (A) annotationMap.get(annotationType);
   }

   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      if (metaAnnotationMap == null)
      {
         metaAnnotationMap = new MetaAnnotationMap();
      }
      metaAnnotationMap = populateMetaAnnotationMap(metaAnnotationType, metaAnnotationMap, annotationMap);
      return metaAnnotationMap.get(metaAnnotationType);
   }

   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      if (annotationArray == null)
      {
         annotationArray = new Annotation[0];
         annotationArray = getMetaAnnotations(metaAnnotationType).toArray(annotationArray);
      }
      return annotationArray;
   }

   public Set<Annotation> getAnnotations()
   {
      if (annotationSet == null)
      {
         annotationSet = new HashSet<Annotation>();
         annotationSet.addAll(annotationMap.values());
      }
      return annotationSet;
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotatedType)
   {
      return annotationMap.containsKey(annotatedType);
   }

   protected static <A extends Annotation> MetaAnnotationMap populateMetaAnnotationMap(Class<A> metaAnnotationType, MetaAnnotationMap metaAnnotationMap, AnnotationMap annotationMap)
   {
      if (!metaAnnotationMap.containsKey(metaAnnotationType))
      {
         Set<Annotation> s = new HashSet<Annotation>();
         for (Entry<Class<? extends Annotation>, Annotation> entry : annotationMap.entrySet())
         {
            if (entry.getKey().isAnnotationPresent(metaAnnotationType))
            {
               s.add(entry.getValue());
            }
         }
         metaAnnotationMap.put(metaAnnotationType, s);
      }
      return metaAnnotationMap;
   }

   protected AnnotationMap getAnnotationMap()
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
      if (getMetaAnnotations(BindingType.class).size() > 0)
      {
         return getMetaAnnotations(BindingType.class);
      }
      else
      {
         return DEFAULT_BINDING;
      }
   }

   public Annotation[] getBindingTypesAsArray()
   {
      if (getMetaAnnotationsAsArray(BindingType.class).length > 0)
      {
         return getMetaAnnotationsAsArray(BindingType.class);
      }
      else
      {
         return DEFAULT_BINDING_ARRAY;
      }
   }

   public boolean isProxyable()
   {
      if (Reflections.getConstructor(getType()) == null)
      {
         return false;
      }
      else if (Reflections.isTypeOrAnyMethodFinal(getType()))
      {
         return false;
      }
      else if (Reflections.isPrimitive(getType()))
      {
         return false;
      }
      else if (Reflections.isArrayType(getType()))
      {
         return false;
      }
      else
      {
         return true;
      }
   }

}