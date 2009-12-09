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
package org.jboss.weld.bean.builtin.ee;

import static org.jboss.weld.logging.messages.BeanMessage.BEAN_NOT_EE_RESOURCE_PRODUCER;
import static org.jboss.weld.logging.messages.BeanMessage.INVALID_RESOURCE_PRODUCER_FIELD;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.ForbiddenStateException;
import org.jboss.weld.WeldException;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.builtin.CallableMethodHandler;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.ws.WSApiAbstraction;

/**
 * @author pmuir
 *
 */
public class EEResourceProducerField<X, T> extends ProducerField<X, T>
{
   
   
   private static class EEResourceCallable<T> extends AbstractEECallable<T>
   {
      
      private static final long serialVersionUID = 6287931036073200963L;
      
      private final String beanId;
      private transient T instance;
      private final CreationalContext<T> creationalContext;

      public EEResourceCallable(BeanManagerImpl beanManager, ProducerField<?, T> producerField, CreationalContext<T> creationalContext)
      {
         super(beanManager);
         this.beanId = producerField.getId();
         this.creationalContext = creationalContext;
      }

      public T call() throws Exception
      {
         if (instance == null)
         {
            Contextual<T> contextual = Container.instance().deploymentServices().get(ContextualStore.class).<Contextual<T>, T>getContextual(beanId);
            if (contextual instanceof EEResourceProducerField<?, ?>)
            {
               @SuppressWarnings("unchecked")
               EEResourceProducerField<?, T> bean = (EEResourceProducerField<?, T>) contextual;
               
               this.instance = bean.createUnderlying(creationalContext);
            }
            else
            {
               throw new ForbiddenStateException(BEAN_NOT_EE_RESOURCE_PRODUCER, contextual);
            }
         }
         return instance;
      }
      
      @Override
      public String toString()
      {
         return beanId;
      }
      
   }
   
   /**
    * Creates an EE resource producer field
    * 
    * @param field The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param manager the current manager
    * @return A producer field
    */
   public static <X, T> EEResourceProducerField<X, T> of(WeldField<T, X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager)
   {
      return new EEResourceProducerField<X, T>(field, declaringBean, manager);
   }

   protected EEResourceProducerField(WeldField<T, X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager)
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
         throw new ForbiddenStateException(INVALID_RESOURCE_PRODUCER_FIELD, getAnnotatedItem());
      }
   }
   
   @Override
   public T create(CreationalContext<T> creationalContext)
   {
      try
      {
         if (Reflections.isFinal(getAnnotatedItem().getJavaClass()) || Serializable.class.isAssignableFrom(getAnnotatedItem().getJavaClass()))
         {
            return createUnderlying(creationalContext);
         }
         else
         {
            return Proxies.<T>createProxy(new CallableMethodHandler(new EEResourceCallable<T>(getManager(), this, creationalContext)), TypeInfo.of(getTypes()).add(Serializable.class));
         }
      }
      catch (InstantiationException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_FAILED, e, this);
      }
      catch (IllegalAccessException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
      }
   }
   
   /**
    * Access to the underlying producer field
    */
   private T createUnderlying(CreationalContext<T> creationalContext)
   {
      return super.create(creationalContext);
   }
   
   @Override
   public boolean isPassivationCapableBean()
   {
      return true;
   }


}
