/*
 * JBoss, Home of Professional Open Source
 * Copyright <Year>, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.bean;

import javax.enterprise.inject.spi.Decorator;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.resources.ClassTransformer;

/**
 * A wrapper for a decorated instance. Allows to enhance custom decorators with metadata
 * about the WeldClass at deployment time.
 *
 * @author Marius Bogoevici
 */
public class AnnotatedItemProvidingDecoratorWrapper extends ForwardingDecorator<Object>
{
   private Decorator<Object> delegate;
   private WeldClass<?> annotatedItem;

   public static AnnotatedItemProvidingDecoratorWrapper of(Decorator<?> delegate, BeanManagerImpl beanManager)
   {
      return new AnnotatedItemProvidingDecoratorWrapper((Decorator<Object>) delegate, beanManager);
   }

   private AnnotatedItemProvidingDecoratorWrapper(Decorator<Object> delegate, BeanManagerImpl beanManager)
   {
      this.delegate = delegate;
      ClassTransformer transformer = beanManager.getServices().get(ClassTransformer.class);
      Class<?> beanClass = delegate.getBeanClass();
      this.annotatedItem =  transformer.loadClass(beanClass);
   }

   @Override
   protected Decorator<Object> delegate()
   {
      return delegate;
   }

   public WeldClass<?> getAnnotatedItem()
   {
      return annotatedItem;
   }
}
