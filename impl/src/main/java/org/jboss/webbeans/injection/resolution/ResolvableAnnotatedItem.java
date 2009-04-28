/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.injection.resolution;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;

/**
 * Extension of an element which bases equality not only on type, but also on
 * binding type
 */
abstract class ResolvableAnnotatedItem<T, S> extends ForwardingAnnotatedItem<T, S>
{
   
   private static final long serialVersionUID = 1L;

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AnnotatedItem)
      {
         AnnotatedItem<?, ?> that = (AnnotatedItem<?, ?>) other;
         return delegate().isAssignableFrom(that) && that.getBindings().equals(this.getBindings());
      }
      else
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }

   @Override
   public String toString()
   {
      return "Resolvable annotated item for " + delegate();
   }

}