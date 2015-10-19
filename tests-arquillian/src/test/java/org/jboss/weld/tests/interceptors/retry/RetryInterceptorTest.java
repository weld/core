/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.tests.interceptors.retry;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Broken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Marius Bogoevici
 */
@RunWith(Arquillian.class)
public class RetryInterceptorTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(RetryInterceptorTest.class))
                .intercept(RetryInterceptor.class, SecuredInterceptor.class)
                .addPackage(RetryInterceptorTest.class.getPackage());
    }

    @Test
    @Category(Broken.class) // WELD-1244
    public void testRetry(Processor processor) {
        FailingProcessor.intercepts = 0;
        RetryInterceptor.invocationCount = 0;
        System.out.println(processor);
        Assert.assertEquals(3, processor.tryToProcess());
        Assert.assertEquals(1, TransactionalInterceptor.invocationCount);
        Assert.assertEquals(3, RetryInterceptor.invocationCount);
        Assert.assertEquals(3, SecuredInterceptor.invocationCount);
        Assert.assertEquals(3, FailingProcessor.intercepts);
    }

}
