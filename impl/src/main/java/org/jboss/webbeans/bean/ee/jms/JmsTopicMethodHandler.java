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

import javax.inject.ExecutionException;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.messaging.spi.JmsServices;

/**
 * @author Pete Muir
 *
 */
class JmsTopicMethodHandler extends JmsMethodHandler<TopicConnection, TopicSession, TopicPublisher, TopicSubscriber>
{

   private static final long serialVersionUID = 5209925842290226027L;
   
   private final ConnectionContextual<TopicConnection> connectionContexual;
   private final SessionContextual<TopicSession> sessionContextual;
   private final MessageProducerContextual<TopicPublisher> messageProducerContextual;
   private final MessageConsumerContextual<TopicSubscriber> messageConsumerContextual;

   /**
    * @param jndiName
    * @param mappedName
    */
   public JmsTopicMethodHandler(String jndiName, String mappedName)
   {
      super(jndiName, mappedName);
      final JmsServices jmsServices = CurrentManager.rootManager().getServices().get(JmsServices.class);
      this.connectionContexual = new ConnectionContextual<TopicConnection>()
      {

         private static final long serialVersionUID = 7830020942920371399L;

         @Override
         protected TopicConnection createConnection() throws JMSException
         {
            return jmsServices.getTopicConnectionFactory().createTopicConnection();
         }

      };
      this.sessionContextual = new SessionContextual<TopicSession>()
      {

         private static final long serialVersionUID = -5964106446504141417L;

         @Override
         protected TopicSession createSession() throws JMSException
         {
            return createSessionFromConnection(connectionContexual);
         }

      };
      this.messageProducerContextual = new MessageProducerContextual<TopicPublisher>()
      {

         private static final long serialVersionUID = 3215720243380210179L;

         @Override
         protected TopicPublisher createMessageProducer() throws JMSException
         {
            Topic topic = jmsServices.resolveDestination(getJndiName(), getMappedName());
            try
            {
               return createSessionFromConnection(connectionContexual).createPublisher(topic);
            }
            catch (JMSException e)
            {
               throw new ExecutionException("Error creating TopicPublisher", e);
            }
         }

      };
      this.messageConsumerContextual = new MessageConsumerContextual<TopicSubscriber>()
      {

         private static final long serialVersionUID = -5461921479716229659L;

         @Override
         protected TopicSubscriber createMessageConsumer() throws JMSException
         {
            Topic topic = jmsServices.resolveDestination(getJndiName(), getMappedName());
            try
            {
               return createSessionFromConnection(connectionContexual).createSubscriber(topic);
            }
            catch (JMSException e)
            {
               throw new ExecutionException("Error creating TopicSubscriber", e);
            }
         }

      };
   }

   @Override
   protected TopicSession createSessionFromConnection(ConnectionContextual<TopicConnection> connectionContexual)
   {
      try
      {
         return getConnection(connectionContexual).createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      }
      catch (JMSException e)
      {
         throw new ExecutionException("Error creating session", e);
      }
   }

   @Override
   protected ConnectionContextual<TopicConnection> getConnectionContextual()
   {
      return connectionContexual;
   }

   @Override
   protected MessageConsumerContextual<TopicSubscriber> getMessageConsumerContextual()
   {
      return messageConsumerContextual;
   }

   @Override
   protected MessageProducerContextual<TopicPublisher> getMessageProducerContextual()
   {
      return messageProducerContextual;
   }

   @Override
   protected SessionContextual<TopicSession> getSessionContextual()
   {
      return sessionContextual;
   }
   
   

}
