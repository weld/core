package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;


/**
 * AnnotatedItem provides a uniform access to the annotations on an annotated
 * item defined either in Java or XML 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedItem
{

   /**
    * Get all annotations on the item
    * 
    * An empty set is returned if no annotations are present
    */
   public abstract <T extends Annotation> Set<T> getAnnotations();

   /**
    * Get all annotations which are annotated with the given meta annotation 
    * type
    * 
    * If no annotations are present which are annotated with the given meta
    * annotation an empty set is returned
    */
   public abstract <T extends Annotation> Set<Annotation> getAnnotations(
         Class<T> metaAnnotationType);

   /**
    * Get an annotation for the annotation type specified.
    * 
    * If the annotation isn't present, null is returned
    */
   public abstract <T extends Annotation> T getAnnotation(
         Class<T> annotationType);

   /**
    * Return true if the annotation type specified is present
    */
   public abstract boolean isAnnotationPresent(
         Class<? extends Annotation> annotationType);

}