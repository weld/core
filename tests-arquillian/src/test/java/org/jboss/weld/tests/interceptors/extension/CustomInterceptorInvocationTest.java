/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.extension;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Registers an extension-provided implementation of the {@link Interceptor} interface and verifies that the implementation is
 * invoked upon invocation of an intercepted method.
 *
 * @see WELD-997
 *
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 *
 */
@RunWith(Arquillian.class)
public class CustomInterceptorInvocationTest {

    @Inject
    private InterceptedBean bean;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(CustomInterceptorInvocationTest.class))
                .intercept(FooInterceptor.class)
                .addClasses(AbstractInterceptor.class, CustomInterceptor.class, CustomInterceptorExtension.class,
                        FooInterceptor.class, FooInterceptorBinding.class, InterceptedBean.class) // do not bundle
                // InterceptedSerializableBean
                .addAsServiceProvider(Extension.class, CustomInterceptorExtension.class);
    }

    @Test
    public void testCustomInterceptorInvocation() {
        CustomInterceptor.reset();
        FooInterceptor.reset();
        bean.foo();
        assertTrue(CustomInterceptor.isInvoked());
        assertTrue(FooInterceptor.isInvoked());
    }
}
