/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.selfInvocation;

import jakarta.enterprise.event.Event;
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

@RunWith(Arquillian.class)
public class SelfInvocationInterceptionTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SelfInvocationInterceptionTest.class))
                .addPackage(SelfInvocationInterceptionTest.class.getPackage());
    }

    @Inject
    SomeBean bean;

    @Inject
    Event<Object> event;

    @Test
    public void testInterceptionViaObserverMethod() {
        // this call goes through private final observer, directly calling intercepted method
        // should not trigger interceptor
        MyOtherInterceptor.resetCounter();
        event.select(String.class).fire("bar");
        Assert.assertEquals(0, MyOtherInterceptor.TIMES_INVOKED);
    }

    @Test
    public void testInterceptionViaObserverMethodWithParams() {
        // this call goes through private final observer with additional IP, directly calling intercepted method
        // should not trigger interceptor
        MyOtherInterceptor.resetCounter();
        event.select(Integer.class).fire(1);
        Assert.assertEquals(0, MyOtherInterceptor.TIMES_INVOKED);
    }

    @Test
    public void testInterceptionViaObserverMethodWithBMParam() {
        // this call goes through private final observer with additional IP, directly calling intercepted method
        // should not trigger interceptor
        MyOtherInterceptor.resetCounter();
        event.select(Double.class).fire(1.0);
        Assert.assertEquals(0, MyOtherInterceptor.TIMES_INVOKED);
    }

    @Test
    public void testInterceptionViaObserverMethodWithMetadata() {
        // this call goes through private final observer with additional IP, directly calling intercepted method
        // should not trigger interceptor
        MyOtherInterceptor.resetCounter();
        event.select(Float.class).fire(1.0f);
        Assert.assertEquals(0, MyOtherInterceptor.TIMES_INVOKED);
    }

    @Test
    public void testDirectMethodInvocation() {
        MyInterceptor.resetCounter();
        bean.invokePrivateInterceptedDirectly();
        // should not trigger interceptor
        Assert.assertEquals(0, MyInterceptor.TIMES_INVOKED);
    }

    @Test
    public void testInvocationViaProxy() {
        MyInterceptor.resetCounter();
        bean.invokePrivateInterceptedViaProxy();
        // should work
        Assert.assertEquals(1, MyInterceptor.TIMES_INVOKED);
    }
}
