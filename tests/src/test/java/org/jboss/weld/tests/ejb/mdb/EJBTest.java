/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.ejb.mdb;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@IntegrationTest
public class EJBTest extends AbstractWeldTest
{
   
   public static final String MESSAGE = "Hello!";
   
   @Test(groups="broken")
   // TODO Need a way to deploy test-destinations-service.xml to JBoss AS
   public void testMdbUsable() throws Exception
   {
      InitialContext ctx = new InitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection connection = factory.createQueueConnection();
      QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) ctx.lookup("queue/testQueue");
      QueueSender sender = session.createSender(queue);
      sender.send(session.createTextMessage(MESSAGE));
      Thread.sleep(1000);
      assert getReference(Control.class).isMessageDelivered();
      assert getReference(Control.class).isContextSet();
   }
   
}
