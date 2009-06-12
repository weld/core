package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;

import org.jboss.webbeans.introspector.AnnotatedItem;

public class AnnotatedAdaptor implements Annotated
{

   private final AnnotatedItem<?, ?> annotatedItem;
   
   public AnnotatedAdaptor(AnnotatedItem<?, ?> annotatedItem)
   {
      this.annotatedItem = annotatedItem;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return annotatedItem.getAnnotation(annotationType);
   }

   public Set<Annotation> getAnnotations()
   {
      return annotatedItem.getAnnotations();
   }

   public Type getType()
   {
      return annotatedItem.getType();
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotationType.isAnnotationPresent(annotationType);
   }

   
   
}
