package org.jboss.weld.tests.util.annotated;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Stuart Douglas
 *
 */
class TestAnnotationStore
{
   
   private final HashMap<Class<? extends Annotation>, Annotation> annotationMap;
   private final Set<Annotation> annotationSet;

   TestAnnotationStore(HashMap<Class<? extends Annotation>, Annotation> annotationMap, Set<Annotation> annotationSet)
   {
      this.annotationMap = annotationMap;
      this.annotationSet = annotationSet;
   }
   
   TestAnnotationStore()
   {
      this.annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      this.annotationSet = new HashSet<Annotation>();
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return annotationType.cast(annotationMap.get(annotationType));
   }

   public Set<Annotation> getAnnotations()
   {
      return Collections.unmodifiableSet(annotationSet);
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotationMap.containsKey(annotationType);
   }

}
