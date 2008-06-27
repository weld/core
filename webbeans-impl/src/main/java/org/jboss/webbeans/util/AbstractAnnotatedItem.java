package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Base class for implementing AnnotatedItem. This implementation assumes 
 * the annotationMap is immutable.
 * 
 * @author pmuir
 *
 */
public abstract class AbstractAnnotatedItem implements AnnotatedItem
{
   
   private Map<Class<? extends Annotation>, Annotation> annotationMap;
   private Map<Class<? extends Annotation>, Set<Annotation>> metaAnnotations;
   private Set<Annotation> annotationSet;
   private Class<?> annotatedClass;
   
   public AbstractAnnotatedItem(Class<?> annotatedClass, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      this.annotatedClass = annotatedClass;
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return (T) annotationMap.get(annotationType);
   }

   public <T extends Annotation> Set<Annotation> getAnnotations(Class<T> metaAnnotationType)
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

   public Class<?> getAnnotatedClass()
   {
      return annotatedClass;
   }
   
   protected static Set<Annotation> populateAnnotationSet(Set<Annotation> annotationSet, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      for (Entry<Class<? extends Annotation>, Annotation> entry : annotationMap.entrySet())
      {
         annotationSet.add(entry.getValue());
      }
      return annotationSet;
   }
   
   protected static <T extends Annotation> Map<Class<? extends Annotation>, Set<Annotation>> populateMetaAnnotationMap(Class<T> metaAnnotationType, Map<Class<? extends Annotation>, Set<Annotation>> metaAnnotations, Map<Class<? extends Annotation>, Annotation> annotationMap)
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
   public String toString()
   {
      return annotatedClass + " " + annotationMap.toString();
   }

}