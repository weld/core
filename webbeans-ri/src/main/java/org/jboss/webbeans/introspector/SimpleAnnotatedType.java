package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Base class for implementing AnnotatedItem. This implementation assumes 
 * the annotationMap is immutable.
 * 
 * @author pmuir
 *
 */
public class SimpleAnnotatedType extends SimpleAnnotatedItem implements AnnotatedType
{
   
   private Class<?> annotatedClass;
   
   public SimpleAnnotatedType(Class<?> annotatedClass, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
      this.annotatedClass = annotatedClass;
   }
   
   public SimpleAnnotatedType(Class<?> annotatedClass)
   {
      this(annotatedClass, buildAnnotationMap(annotatedClass));
   }
   
   public Class<?> getAnnotatedClass()
   {
      return annotatedClass;
   }
   
   @Override
   public String toString()
   {
      return annotatedClass + " " + super.getAnnotationMap().toString();
   }

}