package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public abstract class AbstractAnnotatedItem<T, S> implements AnnotatedItem<T, S>
{

   private Map<Class<? extends Annotation>, Annotation> annotationMap;
   private Map<Class<? extends Annotation>, Set<Annotation>> metaAnnotations;
   private Set<Annotation> annotationSet;
   
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

   public <A extends Annotation> A getAnnotation(Class<? extends A> annotationType)
   {
      return (A) annotationMap.get(annotationType);
   }

   public Set<Annotation> getAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      if (metaAnnotations == null)
      {
         metaAnnotations = new HashMap<Class<? extends Annotation>, Set<Annotation>>();
      }
      populateMetaAnnotationMap(metaAnnotationType, metaAnnotations, annotationMap);
      return metaAnnotations.get(metaAnnotationType);
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
         Set<Annotation>> metaAnnotations, 
         Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      if (!metaAnnotations.containsKey(metaAnnotationType))
      {
         Set<Annotation> s = new HashSet<Annotation>();
         for (Entry<Class<? extends Annotation>, Annotation> entry : annotationMap.entrySet())
         {
            if (entry.getValue().annotationType().isAnnotationPresent(metaAnnotationType))
            {
               s.add(entry.getValue());
            }
         }
         metaAnnotations.put(metaAnnotationType, s);
      }
      return metaAnnotations;
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
         return this.getAnnotations().equals(that.getAnnotations()) && this.getDelegate().equals(that.getDelegate());
      }
      return false;
   }

}