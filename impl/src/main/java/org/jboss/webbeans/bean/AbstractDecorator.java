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

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;


/**
 * The Bean object for a a decorator
 * 
 * This interface should not be called directly by the application.
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractDecorator extends BaseBean<Object> implements Decorator<Object>
{

   /**
    * Create an interceptor bean
    * 
    * @param beanManager
    *           the manager to create the interceptor for
    */
   protected AbstractDecorator(BeanManager beanManager)
   {
      super(beanManager);
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.Decorator#getDelegateType()
    */
   public abstract Class<?> getDelegateType();

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.Decorator#getDelegateBindings()
    */
   public abstract Set<Annotation> getDelegateBindings();

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.Decorator#setDelegate(java.lang.Object, java.lang.Object)
    */
   public abstract void setDelegate(Object instance, Object delegate);

}
