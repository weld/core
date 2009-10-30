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

package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.event.ObserverMethodImpl;

/**
 * Implementation of the event used to notify observers for each observer
 * method that is added.
 * 
 * @author David Allen
 *
 */
public class ProcessObserverMethodImpl<X, T> extends AbstractDefinitionContainerEvent implements ProcessObserverMethod<X, T>
{
   
   public static <X, T> void fire(BeanManagerImpl beanManager, ObserverMethodImpl<X, T> observer)
   {
      new ProcessObserverMethodImpl<X, T>(beanManager, observer.getMethod(), observer) {}.fire();
   }
   
   private final AnnotatedMethod<X>   beanMethod;
   private final ObserverMethod<T> observerMethod;
   
   public ProcessObserverMethodImpl(BeanManagerImpl beanManager, AnnotatedMethod<X> beanMethod, ObserverMethodImpl<X, T> observerMethod)
   {
      super(beanManager, ProcessObserverMethod.class, new Type[] { observerMethod.getMethod().getDeclaringType().getBaseType(), observerMethod.getObservedType() });
      this.beanMethod = beanMethod;
      this.observerMethod = observerMethod;
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }

   public AnnotatedMethod<X> getAnnotatedMethod()
   {
      return beanMethod;
   }

   public ObserverMethod<T> getObserverMethod()
   {
      return observerMethod;
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return Collections.unmodifiableList(getErrors());
   }

}
