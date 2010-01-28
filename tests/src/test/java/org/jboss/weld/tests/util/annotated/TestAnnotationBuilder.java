package org.jboss.weld.tests.util.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Stuart Douglas
 *
 */
class TestAnnotationBuilder
{
   private HashMap<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
   private Set<Annotation> annotationSet = new HashSet<Annotation>();

   public TestAnnotationBuilder add(Annotation a)
   {
      annotationSet.add(a);
      annotationMap.put(a.getClass(), a);
      return this;
   }

   public TestAnnotationStore create()
   {
      return new TestAnnotationStore(annotationMap, annotationSet);
   }
   
   public TestAnnotationBuilder addAll(Set<Annotation> annotations)
   {
      for (Annotation annotation : annotations)
      {
         add(annotation);
      }
      return this;
   }
   
   public TestAnnotationBuilder addAll(TestAnnotationStore annotations)
   {
      for (Annotation annotation : annotations.getAnnotations())
      {
         add(annotation);
      }
      return this;
   }

   public TestAnnotationBuilder addAll(AnnotatedElement element)
   {
      for (Annotation a : element.getAnnotations())
      {
         add(a);
      }
      return this;
   }

}
