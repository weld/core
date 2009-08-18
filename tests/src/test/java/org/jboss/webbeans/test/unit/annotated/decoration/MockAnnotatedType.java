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
package org.jboss.webbeans.test.unit.annotated.decoration;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
class MockAnnotatedType<X> extends MockAnnotated implements AnnotatedType<X>
{
   private final Set<AnnotatedConstructor<X>> annotatedConstructors;
   
   private final Set<AnnotatedField<? super X>> annotatedFields;
   
   private final Set<AnnotatedMethod<? super X>> annotatedMethods;
   
   MockAnnotatedType(AnnotatedType<X> delegate)
   {
      super(delegate);
      annotatedConstructors = initialiseConstructors();
      annotatedFields = initialiseAnnotatedFields();
      annotatedMethods = initialiseMethods();
   }
   
   private Set<AnnotatedField<? super X>> initialiseAnnotatedFields()
   {
      Set<AnnotatedField<? super X>> fields = new HashSet<AnnotatedField<? super X>>();
      for (AnnotatedField<? super X> field : getDelegate().getFields())
      {
         if (field.isStatic())
         {
            fields.add(field);
         }
         else
         {
            fields.add(new MockAnnotatedField<X>(field));
         }
      }
      return fields;
   }
   
   private Set<AnnotatedConstructor<X>> initialiseConstructors()
   {
      Set<AnnotatedConstructor<X>> constructors = new HashSet<AnnotatedConstructor<X>>();
      for (AnnotatedConstructor<X> constructor : getDelegate().getConstructors())
      {
         constructors.add(new MockAnnotatedConstructor<X>(constructor));
      }
      return constructors;
   }
   
   private Set<AnnotatedMethod<? super X>> initialiseMethods()
   {
      Set<AnnotatedMethod<? super X>> methods = new HashSet<AnnotatedMethod<? super X>>();
      for (AnnotatedMethod<? super X> method : getDelegate().getMethods())
      {
         if (method.isStatic())
         {
            methods.add(method);
         }
         else
         {
            methods.add(new MockAnnotatedMethod<X>(method));
         }
      }
      return methods;
   }
   
   @Override
   AnnotatedType<X> getDelegate()
   {
      return (AnnotatedType<X>)super.getDelegate();
   }
   
   public Set<AnnotatedConstructor<X>> getConstructors()
   {
      return annotatedConstructors;
   }

   public Set<AnnotatedField<? super X>> getFields()
   {
      return annotatedFields;
   }

   public Set<AnnotatedMethod<? super X>> getMethods()
   {
      return annotatedMethods;
   }

   public Class<X> getJavaClass()
   {
      return getDelegate().getJavaClass();
   }
}
