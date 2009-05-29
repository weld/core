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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public class CreationalContextImpl<T> implements CreationalContext<T>
{
   
   private final Map<Bean<?>, Object> incompleteInstances;
   private final Bean<T> bean;
   private final boolean outer;
   
   public CreationalContextImpl(Bean<T> bean)
   {
      this.incompleteInstances = new HashMap<Bean<?>, Object>();
      this.bean = bean;
      this.outer = true;
   }
   
   private CreationalContextImpl(Bean<T> bean, Map<Bean<?>, Object> incompleteInstances)
   {
      this.incompleteInstances = incompleteInstances;
      this.bean = bean;
      this.outer = false;
   }
   
   public void push(T incompleteInstance)
   {
      incompleteInstances.put(bean, incompleteInstance);
   }
   
   public <S> CreationalContextImpl<S> getCreationalContext(Bean<S> bean)
   {
      return new CreationalContextImpl<S>(bean, new HashMap<Bean<?>, Object>(incompleteInstances));
   }
   
   public <S> S getIncompleteInstance(Bean<S> bean)
   {
      return (S) incompleteInstances.get(bean);
   }
   
   public boolean containsIncompleteInstance(Bean<?> bean)
   {
      return incompleteInstances.containsKey(bean);
   }
   
   public boolean isOuter()
   {
      return outer;
   }

   public void release()
   {
      // TODO Auto-generated method stub
      
   }
   
}
