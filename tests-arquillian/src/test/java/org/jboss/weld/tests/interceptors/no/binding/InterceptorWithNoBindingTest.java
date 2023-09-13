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
package org.jboss.weld.tests.interceptors.no.binding;

import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.Interceptors;

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
 * Verifies, that it is OK for an interceptor bound using the {@link Interceptors} annotation
 * to be also annotated with {@link Interceptor} and declare no interceptor bindings.
 *
 * @see WELD-1668
 * @see INTERCEPTORS_SPEC-27
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class InterceptorWithNoBindingTest {

    @Inject
    private Foo foo;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorWithNoBindingTest.class))
                .addPackage(InterceptorWithNoBindingTest.class.getPackage());
    }

    @Test
    public void testInterceptorWithNoBindingsInvoked() {
        Assert.assertTrue(foo.isIntercepted());
    }
}
