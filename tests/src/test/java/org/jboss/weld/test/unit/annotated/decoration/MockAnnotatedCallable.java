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
package org.jboss.weld.test.unit.annotated.decoration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.inject.Inject;

import org.jboss.annotation.factory.AnnotationCreator;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class MockAnnotatedCallable<X> extends MockAnnotatedMember<X> implements AnnotatedCallable<X>
{
   private final static Inject INITIALIZER;
   static
   {
      try
      {
         INITIALIZER = (Inject)AnnotationCreator.createAnnotation("@" + Inject.class.getName(), Inject.class);
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private final List<AnnotatedParameter<X>> parameters;

   public MockAnnotatedCallable(Annotated delegate)
   {
      super(delegate);
      parameters = initialiseParameters();
   }

   @Override
   AnnotatedCallable<X> getDelegate()
   {
      return (AnnotatedCallable<X>) super.getDelegate();
   }
   
   private List<AnnotatedParameter<X>> initialiseParameters()
   {
      int size = getDelegate().getParameters().size();
      List<AnnotatedParameter<X>> params = new ArrayList<AnnotatedParameter<X>>(size);
      if (size > 0)
      {
         for (AnnotatedParameter<X> param : getDelegate().getParameters())
         {
            params.add(new MockAnnotatedParameter<X>(param, this));
         }
      }
      return params;
   }

   public List<AnnotatedParameter<X>> getParameters()
   {
      return parameters;
   }
   
   @Override
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      if (annotationType == Inject.class)
      {
         return (T)INITIALIZER;
      }
      return null;
   }

   @Override
   public Set<Annotation> getAnnotations()
   {
      return Collections.singleton((Annotation)INITIALIZER);
   }

   @Override
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotationType == Inject.class;
   }
}
