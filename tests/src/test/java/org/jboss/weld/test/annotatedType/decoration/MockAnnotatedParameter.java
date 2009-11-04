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

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class MockAnnotatedParameter<X> extends MockAnnotated implements AnnotatedParameter<X>
{
   private final AnnotatedCallable<X> callable;
   
   public MockAnnotatedParameter(Annotated delegate, AnnotatedCallable<X> callable)
   {
      super(delegate);
      this.callable = callable;
   }

   @Override
   AnnotatedParameter<X> getDelegate()
   {
      return (AnnotatedParameter<X>)super.getDelegate();
   }

   public AnnotatedCallable<X> getDeclaringCallable()
   {
      return callable;
   }

   public int getPosition()
   {
      return getDelegate().getPosition();
   }

}
