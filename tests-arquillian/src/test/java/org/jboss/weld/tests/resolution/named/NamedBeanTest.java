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
package org.jboss.weld.tests.resolution.named;

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

/**
 * @author Dan Allen
 */
@RunWith(Arquillian.class)
public class NamedBeanTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NamedBeanTest.class))
                .addPackage(NamedBeanTest.class.getPackage());
    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test
    public void testGetNamedBeanWithBinding() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("namedBeanWithBinding"));
        NamedBeanWithBinding instance = (NamedBeanWithBinding) beanManager.getReference(bean, Object.class,
                beanManager.createCreationalContext(bean));
        Assert.assertNotNull(instance);
    }

    /*
     * description = "WELD-435"
     */
    @Test
    public void testNamedInjectedFieldUsesFieldName(NamedBeanConsumer consumer) {
        Assert.assertNotNull(consumer.getFoo());
    }

}
