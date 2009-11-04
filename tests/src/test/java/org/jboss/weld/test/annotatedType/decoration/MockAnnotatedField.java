/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors. 
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/ 
package org.jboss.weld.test.annotatedType.decoration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
class MockAnnotatedField<X> extends MockAnnotatedMember<X> implements AnnotatedField<X>
{
   MockAnnotatedField(AnnotatedField<? super X> delegate)
   {
      super(delegate);
   }

   @Override
   AnnotatedField<X> getDelegate()
   {
      return (AnnotatedField<X>)super.getDelegate();
   }

   public Field getJavaMember()
   {
      return getDelegate().getJavaMember();
   }

   private boolean isDecoratedField()
   {
      return getJavaMember().getName().equals("fromField");
   }
   @Override
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      if (isDecoratedField())
         return super.getAnnotation(annotationType);
      return null;
   }

   @Override
   public Set<Annotation> getAnnotations()
   {
      if (isDecoratedField())
         return super.getAnnotations();
      return Collections.emptySet();
   }

   @Override
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      if (isDecoratedField())
         return super.isAnnotationPresent(annotationType);
      return false;
   }
}
