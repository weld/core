package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Support for a mutable annotated item
 *
 */
public class MutableAnnotatedItem extends AbstractAnnotatedItem
{
   
   private Map<Class<? extends Annotation>, Set<Annotation>> metaAnnotations;
   private Set<Annotation> annotationSet;
   
   public MutableAnnotatedItem(Class<?> annotatedClass, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotatedClass, annotationMap);
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
      populateMetaAnnotationMap(metaAnnotationType, metaAnnotations, getAnnotationMap());
      return metaAnnotations.get(metaAnnotationType);
   }
   
   public Set<Annotation> getAnnotations()
   {
      if (annotationSet == null)
      {
         annotationSet = new HashSet<Annotation>();
         populateAnnotationSet(annotationSet, getAnnotationMap());
      }
      return annotationSet;
   }
   
   /**
    * Add an annotation to the AnnotatedElement
    * @param annotation
    */
   public void add(Annotation annotation)
   {
      setDirty();
      getAnnotationMap().put(annotation.annotationType(), annotation);
   }
   
   public void addAll(Collection<Annotation> annotations)
   {
      for (Annotation annotation : annotations)
      {
         this.getAnnotationMap().put(annotation.annotationType(), annotation);
      }
      setDirty();
   }
   
}
