package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface AnnotatedAnnotation<T extends Annotation> extends AnnotatedType<T>
{
   
   /**
    * Get all annotation members
    * @return
    */
   public Set<AnnotatedMethod<?>> getMembers();
   
   /**
    * Get all annotation members
    * @return
    */
   public Set<AnnotatedMethod<?>> getAnnotatedMembers(Class<? extends Annotation> annotationType);
   
}
