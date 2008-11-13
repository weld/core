package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;


/**
 * Represents a Class
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedClass<T> extends AnnotatedType<T>
{
   
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
    * Get all fields which are meta-annotated with metaAnnotationType 
    * 
    * If no annotations are present which are annotated with the given meta
    * annotation an empty set is returned
    */
   public Set<AnnotatedField<Object>> getMetaAnnotatedFields(
         Class<? extends Annotation> metaAnnotationType);
   
   /**
    * Get all constructors which are annotated with annotationType
    */
   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType);
   
   /**
    * Get all constructors
    * 
    */
   public Set<AnnotatedConstructor<T>> getConstructors();
   
   /**
    * Get the constructor with arguments given
    */
   public AnnotatedConstructor<T> getConstructor(List<Class<?>> arguments);
   
   /**
    * Get all the members annotated with annotationType
    */
   public Set<AnnotatedMethod<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType);
   
   public AnnotatedClass<Object> getSuperclass();
   
}