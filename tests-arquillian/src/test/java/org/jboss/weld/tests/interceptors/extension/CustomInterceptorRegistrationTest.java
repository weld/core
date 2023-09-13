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

import jakarta.interceptor.Interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
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
