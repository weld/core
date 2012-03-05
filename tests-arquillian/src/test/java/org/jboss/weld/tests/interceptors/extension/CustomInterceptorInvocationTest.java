/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.interceptors.extension;

import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
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
                .create(BeanArchive.class)
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
