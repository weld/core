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

package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.webbeans.Instance;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.util.Strings;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T>
 * @see javax.webbeans.Event
 */
public class InstanceImpl<T> extends FacadeImpl<T> implements Instance<T>
{
   /**
    * Constructor
    * 
    * @param type The event type
    * @param manager The Web Beans manager
    * @param bindingTypes The binding types
    */
   public InstanceImpl(Class<T> type, Manager manager, Annotation... bindingTypes)
   {
      super(type, manager, bindingTypes);
   }

   public T get(Annotation... bindingTypes) 
   {
      return manager.getInstanceByType(type, mergeBindings(bindingTypes));
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Obtainable Instance:\n");
      buffer.append(Strings.collectionToString("  Bindings: ", bindingTypes));
      return buffer.toString();
   }

   /**
    * @see org.jboss.webbeans.FacadeImpl#getFilteredAnnotations
    */
   @Override
   protected Set<Class<? extends Annotation>> getFilteredAnnotations()
   {
      return Collections.emptySet();
   }

}
