package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * 
 * @author Stuart Douglas
 *
 */
class TestAnnotatedField<X> extends AbstractTestAnnotatedMember<X, Field> implements AnnotatedField<X>
{

   TestAnnotatedField(AnnotatedType<X> declaringType, Field field, TestAnnotationStore annotations)
   {
      super(declaringType, field, field.getType(), annotations);
   }

}
