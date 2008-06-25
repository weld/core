package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Helper class which allows us to store the annotations present on an object
 * 
 * Also allows you to discover meta annotations
 * 
 * @author pmuir
 *
 */
public class MutableEnhancedAnnotatedElement implements EnhancedAnnotatedElement
{

   private Map<Class<? extends Annotation>, Annotation> annotations;
   
   private Map<Class<? extends Annotation>, Set<Annotation>> metaAnnotations;
   private Set<Annotation> annotationSet;
   
   public MutableEnhancedAnnotatedElement()
   {
      this.annotations = new HashMap<Class<? extends Annotation>, Annotation>();
   }
   
   public MutableEnhancedAnnotatedElement(AnnotatedElement annotatedElement)
   {
      this();
      for (Annotation annotation : annotatedElement.getAnnotations())
      {
         add(annotation);
      }
   }

   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return (T) annotations.get(annotationType);
   }
   
   private void setDirty()
   {
      metaAnnotations = null;
      this.annotationSet = null;
   }
   
   public <T extends Annotation> Set<Annotation> getAnnotations(Class<T> metaAnnotationType)
   {
      if (metaAnnotations == null)
      {
         metaAnnotations = new HashMap<Class<? extends Annotation>, Set<Annotation>>();
      }
      if (!metaAnnotations.containsKey(metaAnnotationType))
      {
         Set<Annotation> s = new HashSet<Annotation>();
         for (Entry<Class<? extends Annotation>, Annotation> entry : annotations.entrySet())
         {
            if (entry.getValue().annotationType().isAnnotationPresent(metaAnnotationType))
            {
               s.add(entry.getValue());
            }
         }
         metaAnnotations.put(metaAnnotationType, s);
      }
      return metaAnnotations.get(metaAnnotationType);
   }
   

   public Set<Annotation> getAnnotations()
   {
      if (annotationSet == null)
      {
         annotationSet = new HashSet<Annotation>();
         for (Entry<Class<? extends Annotation>, Annotation> entry : annotations.entrySet())
         {
            annotationSet.add(entry.getValue());
         }
      }
      return annotationSet;
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotatedType)
   {
      return annotations.containsKey(annotatedType);
   }
   
   /**
    * Add an annotation to the AnnotatedElement
    * @param annotation
    */
   public void add(Annotation annotation)
   {
      setDirty();
      annotations.put(annotation.annotationType(), annotation);
   }
   
   public void addAll(Collection<Annotation> annotations)
   {
      for (Annotation annotation : annotations)
      {
         this.annotations.put(annotation.annotationType(), annotation);
      }
      setDirty();
   }

}
