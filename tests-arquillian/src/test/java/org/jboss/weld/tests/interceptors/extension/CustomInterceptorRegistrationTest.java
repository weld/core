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

import javax.interceptor.Interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Registers an extension-provided implementation of the {@link Interceptor} interface. This causes deployment error on Weld if
 * an intercepted bean is passivation capable.
 * 
 * @see WELD-996
 * @see InterceptedSerializableBean
 * 
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 * 
 */
@RunWith(Arquillian.class)
@Ignore("WELD-996")
public class CustomInterceptorRegistrationTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return CustomInterceptorInvocationTest.getDeployment().addClasses(InterceptedSerializableBean.class,
                CustomInterceptorInvocationTest.class);
    }

    @Test
    public void testCustomInterceptorRegistration() {
        // noop, we verify that the app deploys
    }

}
