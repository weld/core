/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.jboss.weld.tests.producer.field;

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
 * Simple test which invokes a method directly on a normal scoped producer
 * bean to ensure that it's proxy is for that bean and not the product
 * of a producer method.
 *
 * @author David Allen
 */
@RunWith(Arquillian.class)
public class ProducerBeanInvocationTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducerBeanInvocationTest.class))
                .addPackage(ProducerBeanInvocationTest.class.getPackage());
    }

    /*
     * description = "WELD-546"
     */
    @Test
    public void test(Qux bar, QuxProducer producer, @Baz Qux bazBar) {
        Assert.assertEquals("qux", bar.getBar());
        Assert.assertTrue(producer.ping());
        Assert.assertEquals("baz", bazBar.getBar());
    }
}
