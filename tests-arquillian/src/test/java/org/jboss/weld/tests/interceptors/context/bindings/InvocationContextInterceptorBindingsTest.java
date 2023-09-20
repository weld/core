/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.context.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests now deprecated functionality, can be altered/removed once we no longer provide
 * {@link org.jboss.weld.interceptor.WeldInvocationContext#INTERCEPTOR_BINDINGS_KEY}
 * <p/>
 * See https://issues.redhat.com/browse/WELD-2756
 */
@RunWith(Arquillian.class)
public class InvocationContextInterceptorBindingsTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvocationContextInterceptorBindingsTest.class))
                .addPackage(InvocationContextInterceptorBindingsTest.class.getPackage());
    }

    @Test
    public void testAroundInvokeBindings(SimpleBean bean) {
        AroundInvokeInterceptor.reset();
        bean.ping();
        Set<Annotation> bindings = AroundInvokeInterceptor.getContextDataBindings();
        assertEquals(bindings, AroundInvokeInterceptor.getContextBindings());
        assertNotNull(bindings);
        assertEquals(3, bindings.size());
        for (Annotation annotation : bindings) {
            if (annotation.annotationType().equals(BarBinding.class)) {
                BarBinding barBinding = (BarBinding) annotation;
                assertEquals("1", "" + barBinding.age());
            } else if (annotation.annotationType().equals(FooBinding.class)) {
                FooBinding fooBinding = (FooBinding) annotation;
                assertEquals("hello", fooBinding.secret());
            } else if (annotation.annotationType().equals(BazBinding.class)) {
                BazBinding bazBinding = (BazBinding) annotation;
                assertEquals("bye", bazBinding.secret());
            } else {
                fail("Unexpected annotation type");
            }
        }
    }

    @Test
    public void testAroundConstructBindings(Instance<SimpleBean> instance) {
        AroundConstructInterceptor.reset();
        instance.get().ping();
        Set<Annotation> bindings = AroundConstructInterceptor.getContextDataBindings();
        assertEquals(bindings, AroundConstructInterceptor.getContextBindings());
        assertNotNull(bindings);
        assertEquals(2, bindings.size());
        for (Annotation annotation : bindings) {
            if (annotation.annotationType().equals(FooBinding.class)) {
                FooBinding fooBinding = (FooBinding) annotation;
                assertEquals("bla", fooBinding.secret());
            } else if (annotation.annotationType().equals(BazBinding.class)) {
                BazBinding bazBinding = (BazBinding) annotation;
                assertEquals("bye", bazBinding.secret());
            } else {
                fail("Unexpected annotation type");
            }
        }
    }

    @Test
    public void testPostConstructBindings(Instance<SimpleBean> instance) {
        PostConstructInterceptor.reset();
        instance.get().ping();
        Set<Annotation> bindings = PostConstructInterceptor.getContextDataBindings();
        assertEquals(bindings, PostConstructInterceptor.getContextBindings());
        assertNotNull(bindings);
        assertEquals(2, bindings.size());
        for (Annotation annotation : bindings) {
            if (annotation.annotationType().equals(FooBinding.class)) {
                FooBinding fooBinding = (FooBinding) annotation;
                assertEquals("hello", fooBinding.secret());
            } else if (annotation.annotationType().equals(BazBinding.class)) {
                BazBinding bazBinding = (BazBinding) annotation;
                assertEquals("bye", bazBinding.secret());
            } else {
                fail("Unexpected annotation type");
            }
        }
    }

    @Test
    public void testPreDestroyBindings(Instance<SimpleBean> instance) {
        PreDestroyInterceptor.reset();
        instance.get().ping();
        Set<Annotation> bindings = PreDestroyInterceptor.getContextDataBindings();
        assertEquals(bindings, PreDestroyInterceptor.getContextBindings());
        assertNotNull(bindings);
        assertEquals(2, bindings.size());
        for (Annotation annotation : bindings) {
            if (annotation.annotationType().equals(FooBinding.class)) {
                FooBinding fooBinding = (FooBinding) annotation;
                assertEquals("hello", fooBinding.secret());
            } else if (annotation.annotationType().equals(BazBinding.class)) {
                BazBinding bazBinding = (BazBinding) annotation;
                assertEquals("bye", bazBinding.secret());
            } else {
                fail("Unexpected annotation type");
            }
        }
    }
}
