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
package org.jboss.weld.bean.ee;

import javax.persistence.EntityManager;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.introspector.WeldField;

/**
 * @author pmuir
 *
 */
public class PersistenceContextProducerField<X, T extends EntityManager> extends EEResourceProducerField<X, T>
{
   
   /**
    * Creates an EE resource producer field
    * 
    * @param field The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param manager the current manager
    * @return A producer field
    */
   public static <X, T extends EntityManager> EEResourceProducerField<X, T> of(WeldField<T, X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager)
   {
      return new PersistenceContextProducerField<X, T>(field, declaringBean, manager);
   }

   /**
    * @param field
    * @param declaringBean
    * @param manager
    */
   protected PersistenceContextProducerField(WeldField<T, X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager)
   {
      super(field, declaringBean, manager);
   }
   
   @Override
   protected void defaultDispose(T instance) 
   {
      instance.close();
   }

}
