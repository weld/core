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
package org.jboss.weld.tests.ejb.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.interceptor.InvocationContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.module.ejb.SessionBeanInterceptor;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@link SessionBeanInterceptor} works fine.
 *
 * @see WELD-1666
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class SessionBeanInterceptorTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SessionBeanInterceptorTest.class))
                .addPackage(SessionBeanInterceptorTest.class.getPackage());
    }

    @Test
    public void testSessionBeanInterceptor(final BeanManager manager) throws Throwable {
        final SessionBeanInterceptor interceptor = new SessionBeanInterceptor();
        final AtomicReference<Object> result = new AtomicReference<Object>();
        // ctx.proceed() returns true if and only if the request scope is active
        final InvocationContext ctx = new DummyInvocationContext() {
            @Override
            public Object proceed() throws Exception {
                return manager.getContext(RequestScoped.class).isActive();
            }
        };

        // spawn a new thread an run the interceptor in it to verify that the interceptor properly starts the request context
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result.set(interceptor.aroundInvoke(ctx));
                } catch (Exception e) {
                    result.set(e);
                }
            }
        });
        thread.start();
        thread.join();

        if (result.get() instanceof Throwable) {
            throw (Throwable) result.get();
        }
        Assert.assertTrue(Boolean.TRUE.equals(result.get()));
    }

    private static class DummyInvocationContext implements InvocationContext {

        private final Map<String, Object> contextData = new ConcurrentHashMap<String, Object>();

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() throws IllegalStateException {
            return null;
        }

        @Override
        public void setParameters(Object[] params) throws IllegalStateException, IllegalArgumentException {
        }

        @Override
        public Map<String, Object> getContextData() {
            return contextData;
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Object proceed() throws Exception {
            return null;
        }
    }
}
