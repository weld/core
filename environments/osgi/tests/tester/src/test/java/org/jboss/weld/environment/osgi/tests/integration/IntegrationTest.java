/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.environment.osgi.tests.integration;

import org.junit.Ignore;
import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.jboss.weld.environment.osgi.spi.CDIContainerFactory;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.osgi.framework.*;

import java.util.ArrayList;
import java.util.Collection;

import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class IntegrationTest {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.CDIOSGiEnvironment()
        );
    }

    @Test
    //@Ignore
    public void CDIContainerFactoryTest(BundleContext context) throws InterruptedException {
        Environment.waitForEnvironment(context);

        ServiceReference factoryReference = context.getServiceReference(CDIContainerFactory.class.getName());
        CDIContainerFactory factory = (CDIContainerFactory) context.getService(factoryReference);

        Collection<CDIContainer> containers = factory.containers();
        Assert.assertNotNull("The container collection was null",containers);
        Assert.assertEquals("The container collection was not empty",0,containers.size());

        CDIContainer container;
        int i = 0;
        for(Bundle b : context.getBundles()) {
            container = factory.container(b);
            Assert.assertNull("The bundle already got a container",container);
            container = factory.createContainer(b);
            Assert.assertNotNull("The container creation failed", container);
            Assert.assertNull("The container was already added in container collection",factory.container(b));
            factory.addContainer(container);
            container = factory.container(b);
            Assert.assertNotNull("The container cannot be retrieved",container);
            Assert.assertEquals("The container was not correctly added in container collection",++i,containers.size());
        }
        Assert.assertEquals("Too much or too less registered containers",context.getBundles().length,i);

        for(Bundle b : context.getBundles()) {
            factory.removeContainer(b);
            Assert.assertEquals("The container was not correctly removed from container collection",--i,containers.size());
        }
        Assert.assertEquals("There still containers in the container collection",0,i);

    }

    @Test
    //@Ignore
    public void CDIContainerTest(BundleContext context) throws InterruptedException, InvalidSyntaxException {
        Environment.waitForEnvironment(context);

        ServiceReference factoryReference = context.getServiceReference(CDIContainerFactory.class.getName());
        CDIContainerFactory factory = (CDIContainerFactory) context.getService(factoryReference);
        Collection<CDIContainer> containers = factory.containers();
        CDIContainer container = factory.createContainer(context.getBundle());

        Assert.assertEquals("The container had the wrong bundle", context.getBundle(), container.getBundle());
        Assert.assertFalse("The container was declared as STARTED",container.isStarted());
        Assert.assertNotNull("The registration collection was null",container.getRegistrations());
        Assert.assertEquals("The registration collection was not empty",0,container.getRegistrations().size());

        Collection<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();
        container.setRegistrations(registrations);
        Assert.assertNotNull("The registration collection was null",container.getRegistrations());
        Assert.assertEquals("The registration collection was not empty",0,container.getRegistrations().size());

        ServiceRegistration registration = context.registerService(String.class.getName(),"STRING",null);
        registrations.add(registration);
        container.setRegistrations(registrations);
        Assert.assertNotNull("The registration collection was null",container.getRegistrations());
        Assert.assertEquals("The registration collection had the wrong number of registration",1,container.getRegistrations().size());

        registration = context.registerService(String.class.getName(),"STRING2",null);
        registrations.add(registration);
        container.setRegistrations(registrations);
        Assert.assertNotNull("The registration collection was null",container.getRegistrations());
        Assert.assertEquals("The registration collection had the wrong number of registration",2,container.getRegistrations().size());

        registrations.clear();
        container.setRegistrations(registrations);
        Assert.assertNotNull("The registration collection was null",container.getRegistrations());
        Assert.assertEquals("The registration collection was not empty",0,container.getRegistrations().size());

    }
}
