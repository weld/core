/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.thread.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.interceptors.thread.ThreadPool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AsyncInterceptorTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AsyncInterceptorTest.class))
                .addPackage(AsyncInterceptorTest.class.getPackage()).addClass(ThreadPool.class);
    }

    @Inject
    private SimpleBean bean;

    @Test
    public void testNonVoidMethod() throws InterruptedException, ExecutionException {
        Assert.assertTrue(Thread.currentThread() != bean.simpleInvokeAsync().get());
    }

    @Test
    public void testVoidMethod() throws InterruptedException, ExecutionException {
        SynchronousQueue<Thread> synchronizer = new SynchronousQueue<Thread>();
        bean.voidInvokeAsync(synchronizer);
        Assert.assertTrue(Thread.currentThread() != synchronizer.poll());
    }

    @Test
    public void testWithInterceptor() throws Exception {
        Assert.assertEquals(Integer.valueOf(3), bean.invokeAsyncWithOtherInterceptors(0).get(5, TimeUnit.SECONDS));
    }
}
