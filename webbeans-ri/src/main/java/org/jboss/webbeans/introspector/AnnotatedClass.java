package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;


/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedClass<T> extends AnnotatedType<T>
{
   
   /**
    * Return the class of the annotated item. If this annotatedItem isn't in use
    * then this method should return null
    */
   public Class<? extends T> getAnnotatedClass();
   
   /**
    * Get all fields on the type
    * @return
    */
   public Set<AnnotatedField<Object>> getFields();
   
   /**
    * Get all annotations which are annotated with the given annotation 
    * type
    * 
    * If no annotations are present which are annotated with the given
    * annotation an empty set is returned
    */
   public Set<AnnotatedField<Object>> getAnnotatedFields(Class<? extends Annotation> annotationType);
   
   /**
    * Get all fields which are annotated with the given meta annotation 
    * type
    * 
    * If no annotations are present which are annotated with the given meta
    * annotation an empty set is returned
    */
   public Set<AnnotatedField<Object>> getMetaAnnotatedFields(
         Class<? extends Annotation> metaAnnotationType);
   
   /**
    * Get all fields which are annotated with the given meta annotation 
    * type
    * 
    * If no annotations are present which are annotated with the given meta
    * annotation an empty set is returned
    */
   public Set<AnnotatedMethod<Object>> getAnnotatedMethods(
         Class<? extends Annotation> annotationType);
   
}