/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.tests.interceptors.applicationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test for CDI-115 and in the case of JBoss, JBAS-9266
 * Marked as broken since it requires JBoss 6.1.0 to succeed
 *
 * @author Marius Bogoevici
 */

@Category(value = { Integration.class, Broken.class })
@RunWith(Arquillian.class)
public class ApplicationExceptionTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ApplicationExceptionTest.class))
                .intercept(FooInterceptor.class)
                .addPackage(ApplicationExceptionTest.class.getPackage());
    }

    @Test
    public void testSuccessfulMethod(FooStateless stateless) {
        FooInterceptor.invocationCount = 0;
        Assert.assertEquals("hello", stateless.helloWorld());
        Assert.assertEquals(1, FooInterceptor.invocationCount);
    }

    @Test
    public void testFailingMethod(FooStateless stateless) {
        FooInterceptor.invocationCount = 0;
        try {
            stateless.fail();
            Assert.fail("No exception thrown");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FooException);
        }
        Assert.assertEquals(1, FooInterceptor.invocationCount);
    }

}
