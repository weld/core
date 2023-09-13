/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event.lifecycle.ordering;

import java.util.List;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class LifecycleEventOrderingTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(LifecycleEventOrderingTest.class))
                .addPackage(LifecycleEventOrderingTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ProductManagement.class);
    }

    @Inject
    ProductManagement extension;

    @Test
    public void testEventsWereFiredInCorrectOrderForProducer() {
        List<Object> actualListOfEvents = extension.getListOfProducerEvents();
        Assert.assertEquals(4, actualListOfEvents.size());
        Assert.assertTrue(actualListOfEvents.get(0) instanceof ProcessInjectionPoint);
        Assert.assertTrue(actualListOfEvents.get(1) instanceof ProcessProducer);
        Assert.assertTrue(actualListOfEvents.get(2) instanceof ProcessBeanAttributes);
        Assert.assertTrue(actualListOfEvents.get(3) instanceof ProcessProducerMethod);
    }

    @Test
    public void testEventsWereFiredInCorrectOrderForOrdinaryBean() {
        List<Object> actualListOfEvents = extension.getListOfBeanEvents();
        Assert.assertEquals(4, actualListOfEvents.size());
        Assert.assertTrue(actualListOfEvents.get(0) instanceof ProcessInjectionPoint);
        Assert.assertTrue(actualListOfEvents.get(1) instanceof ProcessInjectionTarget);
        Assert.assertTrue(actualListOfEvents.get(2) instanceof ProcessBeanAttributes);
        Assert.assertTrue(actualListOfEvents.get(3) instanceof ProcessManagedBean);
    }
}
