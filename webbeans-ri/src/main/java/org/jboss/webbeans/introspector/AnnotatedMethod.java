package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;

/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedMethod<T> extends AnnotatedItem<T, Method>
{
   
   public Method getAnnotatedMethod();
   
   public List<AnnotatedParameter<Object>> getParameters();
   
   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType);

   public T invoke(ManagerImpl manager, Object instance);
   
   public T invoke(Object instance, Object...parameters);
   
   public Class<?> getDeclaringClass();
   
   public String getPropertyName();

}
