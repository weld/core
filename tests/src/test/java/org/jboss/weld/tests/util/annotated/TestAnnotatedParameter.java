package org.jboss.weld.tests.util.annotated;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

/**
 * 
 * @author Stuart Douglas
 * 
 */
class TestAnnotatedParameter<X> extends AbstractTestAnnotatedElement implements AnnotatedParameter<X>
{

   private final int position;
   private final AnnotatedCallable<X> declaringCallable;

   TestAnnotatedParameter(AnnotatedCallable<X> declaringCallable, Class<?> type, int position, TestAnnotationStore annotations)
   {
      super(type, annotations);
      this.declaringCallable = declaringCallable;
      this.position = position;
   }

   public AnnotatedCallable<X> getDeclaringCallable()
   {
      return declaringCallable;
   }

   public int getPosition()
   {
      return position;
   }

}
