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
package org.jboss.weld.test.unit.decorator.simple;

import javax.decorator.Decorates;
import javax.decorator.Decorator;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Decorator
public class SimpleDecorator2
{
   @Decorates
   SimpleBean delegate;
   
   public static boolean echo2;
   public static boolean echo3;
   
   public static void reset()
   {
      echo2 = false;
      echo3 = false;
   }

   public int echo2(int i)
   {
      echo2 = true;
      return delegate.echo2(i);
   }

   public int echo3(int i)
   {
      echo3 = true;
      return delegate.echo3(i);
   }
}
