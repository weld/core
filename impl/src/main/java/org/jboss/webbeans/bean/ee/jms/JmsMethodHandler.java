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
package org.jboss.webbeans.bean.ee.jms;



import java.lang.reflect.Method;

import javax.context.CreationalContext;
import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jboss.webbeans.bean.ee.AbstractResourceMethodHandler;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;

/**
 * JMS method handler that knows how to create a proxy
 * 
 * @author Pete Muir
 *
 * @param <C> the JMS connection
 * @param <S> the JMS session
 * @param <MP> the JMS message producer
 * @param <MC> the JMS message consumer
 */
abstract class JmsMethodHandler<C extends Connection, S extends Session, MP extends MessageProducer, MC extends MessageConsumer> extends AbstractResourceMethodHandler
{
   
   private static final long serialVersionUID = -2598920314236475437L;
   
   public JmsMethodHandler(String jndiName, String mappedName)
   {
      super(jndiName, mappedName);
   }
   
   @Override
   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      if (method.getName().equals("close"))
      {
         throw new UnsupportedOperationException("Cannot call close on a Web Beans managed JMS resource");
      }
      return super.invoke(self, method, proceed, args);
   }

   @Override
   protected Object getProxiedInstance(Class<?> declaringClass)
   {
      if (Connection.class.isAssignableFrom(declaringClass))
      {
         return getConnection(getConnectionContextual());
      }
      else if (Session.class.isAssignableFrom(declaringClass))
      {
         return getSession(getSessionContextual());
      }
      else if (MessageConsumer.class.isAssignableFrom(declaringClass))
      {
         return getQueueReceiver(getMessageConsumerContextual());
      }
      else if (MessageProducer.class.isAssignableFrom(declaringClass))
      {
         return getMessageProducer(getMessageProducerContextual());
      }
      else
      {
         throw new IllegalStateException("Cannot create proxy for " + declaringClass);
      }
   }
   
   protected abstract ConnectionContextual<C> getConnectionContextual();
   
   protected abstract SessionContextual<S> getSessionContextual();
   
   protected abstract MessageConsumerContextual<MC> getMessageConsumerContextual();
   
   protected abstract MessageProducerContextual<MP> getMessageProducerContextual();
   
   protected abstract S createSessionFromConnection(ConnectionContextual<C> connectionContexual);
   
   protected C getConnection(ConnectionContextual<C> connectionContexual)
   {
      return ApplicationContext.instance().get(connectionContexual, new CreationalContext<C>() 
      {

         public void push(C incompleteInstance) {}
         
      });
   }
   
   private S getSession(SessionContextual<S> sessionContextual)
   {
      return DependentContext.instance().get(sessionContextual, new CreationalContext<S>() 
      {

         public void push(S incompleteInstance) {}
         
      });
   }
   
   private MP getMessageProducer(MessageProducerContextual<MP> messageProducerContextual)
   {
      return DependentContext.instance().get(messageProducerContextual, new CreationalContext<MP>() 
      {

         public void push(MP incompleteInstance) {}
         
      });
   }
   
   private MC getQueueReceiver(MessageConsumerContextual<MC> messageConsumerContextual)
   {
      return DependentContext.instance().get(messageConsumerContextual, new CreationalContext<MC>() 
      {

         public void push(MC incompleteInstance) {}
         
      });
   }
   


}
