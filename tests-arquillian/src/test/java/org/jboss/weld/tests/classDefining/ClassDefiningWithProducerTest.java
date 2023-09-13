/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.classDefining;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.classDefining.a.BeanWithProducer;
import org.jboss.weld.tests.classDefining.b.BeanInterface;
import org.jboss.weld.tests.classDefining.c.AppScopedBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that we are able to define a proxy class for producer method that returns a type from different package.
 * In JDK 11+ this means we need to perform lookup in correct module.
 */
@RunWith(Arquillian.class)
public class ClassDefiningWithProducerTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ClassDefiningWithProducerTest.class))
                .addClass(ClassDefiningWithProducerTest.class)
                .addClass(BeanWithProducer.class)
                .addClass(BeanInterface.class)
                .addClass(AppScopedBean.class);
    }

    @Inject
    BeanWithProducer bean;

    @Test
    public void testProxyDefinitionWorks() {
        // invoke the method
        Assert.assertEquals(666, bean.ping());
        Assert.assertEquals(666, bean.pingNested());
    }
}
