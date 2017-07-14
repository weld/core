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

package org.jboss.weld.tests.interceptors.self;

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
 * @author Marius Bogoevici
 */
@RunWith(Arquillian.class)
public class SelfInvocationTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SelfInvocationTest.class))
                .intercept(SecuredInterceptor.class)
                .decorate(BeanDecorator.class)
                .addPackage(SelfInvocationTest.class.getPackage());
    }

    @Test
    public void testSelfInterception(Bean bean) {
        // safety-check: make sure that intercepted method is intercepted when invoked standalone
        SecuredInterceptor.reset();
        bean.doIntercepted();
        Assert.assertEquals(1, SecuredInterceptor.interceptedInvocations.size());
        Assert.assertTrue(SecuredInterceptor.interceptedInvocations.contains("doIntercepted"));

        // safety-check: make sure that decorated method is decorated when invoked standalone
        BeanDecorator.reset();
        bean.doDecorated();
        Assert.assertEquals(1, BeanDecorator.decoratedInvocationCount);

        // safety-check: make sure that intercepted and decorated methods are not intercepted when invoked from unintercepted method
        SecuredInterceptor.reset();
        BeanDecorator.reset();
        bean.doUnintercepted();
        Assert.assertEquals(0, SecuredInterceptor.interceptedInvocations.size());
        Assert.assertEquals(0, BeanDecorator.decoratedInvocationCount);
    }

    @Test
    public void testSelfInvocationOfInterceptedMethod(SecondBean bean) {
        // safety-check: make sure that intercepted method is intercepted when invoked standalone
        SecuredInterceptor.reset();
        bean.ping();
        Assert.assertEquals(1, SecuredInterceptor.interceptedInvocations.size());
        Assert.assertTrue(SecuredInterceptor.interceptedInvocations.contains("ping"));

        // safety-check: make sure that intercepted method is not intercepted when invoked from unintercepted method
        SecuredInterceptor.reset();
        bean.doUnintercepted();
        Assert.assertEquals(0, SecuredInterceptor.interceptedInvocations.size());
    }

}
