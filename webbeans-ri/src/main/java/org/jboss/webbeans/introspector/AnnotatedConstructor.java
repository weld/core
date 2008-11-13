package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;

/**
 * Represents a Class Constructor
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedConstructor<T> extends AnnotatedItem<T, Constructor<T>>
{
   
   /**
    * Get all parameters to the constructor
    */
   public List<AnnotatedParameter<Object>> getParameters();
   
   /**
    * Get all parameters to the constructor which are annotated with annotationType
    */
   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> annotationType);
   
   /**
    * Create a new instance of the class, using this constructor
    */
   public T newInstance(ManagerImpl manager);
   
   /**
    * 
    */
   public AnnotatedType<T> getDeclaringClass();

}
