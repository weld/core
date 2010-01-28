package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * 
 * @author Stuart Douglas
 * 
 */
abstract class AbstractTestAnnotatedMember<X, M extends Member> extends AbstractTestAnnotatedElement implements AnnotatedMember<X>
{
   private final AnnotatedType<X> declaringType;
   private final M javaMember;

   protected AbstractTestAnnotatedMember(AnnotatedType<X> declaringType, M member, Class<?> memberType, TestAnnotationStore annotations)
   {
      super(memberType, annotations);
      this.declaringType = declaringType;
      this.javaMember = member;
   }

   public AnnotatedType<X> getDeclaringType()
   {
      return declaringType;
   }

   public M getJavaMember()
   {
      return javaMember;
   }

   public boolean isStatic()
   {
      return Modifier.isStatic(javaMember.getModifiers());
   }

}
