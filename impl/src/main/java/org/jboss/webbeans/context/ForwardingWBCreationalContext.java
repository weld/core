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
package org.jboss.webbeans.context;

import javax.enterprise.context.spi.Contextual;


/**
 * @author pmuir
 *
 */
public abstract class ForwardingWBCreationalContext<T> implements WBCreationalContext<T>
{
   
   protected abstract WBCreationalContext<T> delegate();

   public void push(T incompleteInstance)
   {
      delegate().push(incompleteInstance);
   }

   public void release()
   {
      delegate().release();
   }

   public boolean containsIncompleteInstance(Contextual<?> bean)
   {
      return delegate().containsIncompleteInstance(bean);
   }

   public <S> WBCreationalContext<S> getCreationalContext(Contextual<S> Contextual)
   {
      return delegate().getCreationalContext(Contextual);
   }

   public <S> S getIncompleteInstance(Contextual<S> bean)
   {
      return delegate().getIncompleteInstance(bean);
   }

   public DependentInstancesStore getParentDependentInstancesStore()
   {
      return delegate().getParentDependentInstancesStore();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }

}
