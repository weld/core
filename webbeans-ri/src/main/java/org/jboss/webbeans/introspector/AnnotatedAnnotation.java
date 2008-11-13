package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Represents a meta annotation
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedAnnotation<T extends Annotation> extends AnnotatedType<T>
{
   
   /**
    * Get all members
    */
   public Set<AnnotatedMethod<?>> getMembers();
   
   /**
    * Get all the members annotated with annotationType
    */
   public Set<AnnotatedMethod<?>> getAnnotatedMembers(Class<? extends Annotation> annotationType);
   
}
