package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface AnnotatedAnnotation<T extends Annotation> extends AnnotatedItem<T, Class<T>>
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
