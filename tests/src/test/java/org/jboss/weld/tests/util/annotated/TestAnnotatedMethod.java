package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Method;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * 
 * @author Stuart Douglas
 * 
 */
class TestAnnotatedMethod<X> extends AbstractTestAnnotatedCallable<X, Method> implements AnnotatedMethod<X>
{
   TestAnnotatedMethod(AnnotatedType<X> type, Method method, TestAnnotationStore annotations, Map<Integer, TestAnnotationStore> parameterAnnotations)
   {
      super(type, method, method.getReturnType(), method.getParameterTypes(), annotations, parameterAnnotations);
   }

}
