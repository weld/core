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
package org.jboss.weld.tests.producer.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NamedProducerTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NamedProducerTest.class))
                .addPackage(NamedProducerTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test
    public void testNamedProducer() {
        Bean<?> iemonBean = beanManager.resolve(beanManager.getBeans("iemon"));
        String[] iemon = (String[]) beanManager.getReference(iemonBean, Object.class,
                beanManager.createCreationalContext(iemonBean));
        Assert.assertEquals(3, iemon.length);
        Bean<?> itoenBean = beanManager.resolve(beanManager.getBeans("itoen"));
        String[] itoen = (String[]) beanManager.getReference(itoenBean, Object.class,
                beanManager.createCreationalContext(itoenBean));
        Assert.assertEquals(2, itoen.length);
    }

    @Test
    public void testDefaultNamedProducerMethod() {
        Set<Bean<?>> beans = beanManager.getBeans(JmsTemplate.class);
        Assert.assertEquals(2, beans.size());
        List<String> beanNames = new ArrayList<String>(Arrays.asList("errorQueueTemplate", "logQueueTemplate"));
        for (Bean<?> b : beans) {
            beanNames.remove(b.getName());
        }
        Assert.assertTrue(beanNames.isEmpty());
    }

}
