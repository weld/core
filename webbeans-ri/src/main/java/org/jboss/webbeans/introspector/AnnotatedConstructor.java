package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;

/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedConstructor<T> extends AnnotatedItem<T, Constructor<T>>
{
   
   public List<AnnotatedParameter<Object>> getParameters();
   
   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType);
   
   public T newInstance(ManagerImpl manager);
   
   public Class<?> getDeclaringClass();

}
