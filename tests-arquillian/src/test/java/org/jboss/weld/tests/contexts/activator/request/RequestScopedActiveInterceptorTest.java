/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.activator.request;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.context.ContextNotActiveException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class RequestScopedActiveInterceptorTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addClasses(RequestScopedActiveInterceptorTest.class, Foo.class, Bar.class,
                Baz.class);
    }

    private ExecutorService executorService;

    @Before
    public void init() {
        this.executorService = Executors.newFixedThreadPool(1);
    }

    @After
    public void destroy() {
        executorService.shutdown();
    }

    @Test
    public void requestScopedActive(final Foo foo) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return foo.ping();
            }
        });
        assertEquals(Integer.valueOf(1), future.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void requestScopedActiveInNestedInvocation(final Foo foo)
            throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return foo.pingNested();
            }
        });
        assertEquals(Integer.valueOf(3), future.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void requestScopedActiveForAllMethodsInInterceptedClass(final Baz baz)
            throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return baz.ping();
            }
        });
        assertEquals(Integer.valueOf(3), future.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void requestScopedNotActive(final Foo foo) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                try {
                    foo.notInterceptedCall();
                    return 0;
                } catch (ContextNotActiveException e) {
                    return 1;
                }
            }
        });
        assertEquals(Integer.valueOf(1), future.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void requestScopedAlreadyActive(Foo foo) {
        // Should not throw IllegalStateException - more than one context active for scope
        foo.pong();
    }

}
