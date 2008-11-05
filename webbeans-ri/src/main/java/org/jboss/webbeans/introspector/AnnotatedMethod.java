package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

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
   
   public Set<AnnotatedParameter<Object>> getParameters();
   
   public Set<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType);

}
