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
package org.jboss.webbeans.bean.ee;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.ProducerField;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.ws.WSApiAbstraction;

/**
 * @author pmuir
 *
 */
public class EEResourceProducerField<T> extends ProducerField<T>
{
   
   /**
    * Creates an EE resource producer field
    * 
    * @param field The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param manager the current manager
    * @return A producer field
    */
   public static <T> EEResourceProducerField<T> of(WBField<T, ?> field, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      return new EEResourceProducerField<T>(field, declaringBean, manager);
   }

   protected EEResourceProducerField(WBField<T, ?> field, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      super(field, declaringBean, manager);
   }
   
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         super.initialize(environment);
         checkEEResource();
      }
   }
   
   protected void checkEEResource()
   {
      EJBApiAbstraction ejbApiAbstraction = manager.getServices().get(EJBApiAbstraction.class);
      PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);
      WSApiAbstraction wsApiAbstraction = manager.getServices().get(WSApiAbstraction.class);
      if (!(getAnnotatedItem().isAnnotationPresent(ejbApiAbstraction.RESOURCE_ANNOTATION_CLASS) || getAnnotatedItem().isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS) || getAnnotatedItem().isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS) || getAnnotatedItem().isAnnotationPresent(ejbApiAbstraction.EJB_ANNOTATION_CLASS)) || getAnnotatedItem().isAnnotationPresent(wsApiAbstraction.WEB_SERVICE_REF_ANNOTATION_CLASS))
      {
         throw new IllegalStateException("Tried to create an EEResourceProducerField, but no @Resource, @PersistenceContext, @PersistenceUnit, @WebServiceRef or @EJB is present " + getAnnotatedItem());
      }
   }

}
