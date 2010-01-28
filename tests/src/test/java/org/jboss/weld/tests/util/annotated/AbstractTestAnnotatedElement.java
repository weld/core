package org.jboss.weld.tests.util.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;

/**
 * The base class for all New Annotated types.
 * 
 * @author Stuart Douglas
 * 
 */
abstract class AbstractTestAnnotatedElement implements Annotated
{

   private final Class<?> type;
   private final Set<Type> typeClosure;
   private final TestAnnotationStore annotations;

   protected AbstractTestAnnotatedElement(Class<?> type, TestAnnotationStore annotations)
   {
      this.typeClosure = new TestTypeClosureBuilder().add(type).getTypes();
      if (annotations == null)
      {
         this.annotations = new TestAnnotationStore();
      }
      else
      {
         this.annotations = annotations;
      }
      this.type = type;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return annotations.getAnnotation(annotationType);
   }

   public Set<Annotation> getAnnotations()
   {
      return annotations.getAnnotations();
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotations.isAnnotationPresent(annotationType);
   }

   public Set<Type> getTypeClosure()
   {
      return Collections.unmodifiableSet(typeClosure);
   }

   public Type getBaseType()
   {
      return type;
   }
   
}
