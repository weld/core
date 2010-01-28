package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedConstructor;

/**
 * 
 * @author Stuart Douglas
 * 
 */
class TestAnnotatedConstructor<X> extends AbstractTestAnnotatedCallable<X, Constructor<X>> implements AnnotatedConstructor<X>
{

   TestAnnotatedConstructor(TestAnnotatedType<X> type, Constructor<?> constructor, TestAnnotationStore annotations, Map<Integer, TestAnnotationStore> parameterAnnotations)
   {
      super(type, (Constructor<X>) constructor, constructor.getDeclaringClass(), constructor.getParameterTypes(), annotations, parameterAnnotations);
   }

}
