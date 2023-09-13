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

import javax.naming.InitialContext;

import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.WildFly8EEResourceManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class EJBTest {
    public static final String MESSAGE = "Hello!";

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(EnterpriseArchive.class, Utils.getDeploymentNameAsHash(EJBTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModule(
                        ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EJBTest.class))
                                .addPackage(EJBTest.class.getPackage()));
    }

    @Test
    public void testMdbUsable(Control control) throws Exception {
        InitialContext ctx = new InitialContext();
        QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
        QueueConnection connection = factory.createQueueConnection();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = (Queue) ctx.lookup(WildFly8EEResourceManager.TEST_QUEUE_DESTINATION);
        QueueSender sender = session.createSender(queue);
        sender.send(session.createTextMessage(MESSAGE));

        control.getLatch().await();
        Assert.assertTrue(control.isMessageDelivered());
        Assert.assertTrue(control.isContextSet());
    }

}
