package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;


/**
 * AnnotatedItem provides a uniform access to the annotations on an annotated
 * item defined either in Java or XML 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedItem<T, S>
{

   /**
    * Get all annotations on the item
    * 
    * An empty set is returned if no annotations are present
    */
   public <A extends Annotation> Set<A> getAnnotations();

   /**
    * Get all annotations which are annotated with the given meta annotation 
    * type
    * 
    * If no annotations are present which are annotated with the given meta
    * annotation an empty set is returned
    */
   public Set<Annotation> getMetaAnnotations(
         Class<? extends Annotation> metaAnnotationType);
   
   public Annotation[] getMetaAnnotationsAsArray(
         Class<? extends Annotation> metaAnnotationType);
   
   /**
    * Get the binding types for this element
    */
   public Set<Annotation> getBindingTypes();
   
   public Annotation[] getBindingTypesAsArray();

   /**
    * Get an annotation for the annotation type specified.
    * 
    * If the annotation isn't present, null is returned
    */
   public <A extends Annotation> A getAnnotation(
         Class<? extends A> annotationType);

   /**
    * Return true if the annotation type specified is present
    */
   public boolean isAnnotationPresent(
         Class<? extends Annotation> annotationType);
   
   /**
    * Get the underlying element
    * @return
    */
   public S getDelegate();
   
   /**
    * The type of the element
    * @return
    */
   public Class<T> getType();
   
   /**
    * Extends Java Class assignability such that actual type parameters are also 
    * considered
    */
   public boolean isAssignableFrom(AnnotatedItem<?, ?> that);
   
   /**
    * Returns true if any of the types provided are assignable to this, using 
    * the extended assingablity algorithm provided by AnnotatedItem.
    * 
    * The types are assumed to contain their own actual type parameterization.
    */
   public boolean isAssignableFrom(Set<Class<?>> types);
   
   /**
    * Return the actual type arguments for any parameterized types that this
    * AnnotatedItem represents.
    */
   public Type[] getActualTypeArguments();
   
   /**
    * Return true if this AnnotatedItem represents a static element
    */
   public boolean isStatic();
   
   /**
    * Return true if this AnnotatedItem represents a final element
    */
   public boolean isFinal();
   
   /**
    * Return true if this AnnotatedItem can be proxyed
    * @return
    */
   public boolean isProxyable();
   
   /**
    * Return the name of this AnnotatedItem
    * 
    * If it is not possible to determine the name of the underling element, a
    * IllegalArgumentException is thrown
    */
   public String getName();

   

}