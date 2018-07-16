/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.inheritance.packagePrivate;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see WELD-2507
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class PackagePrivateInterceptedTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(PackagePrivateInterceptedTest.class))
                .intercept(SomeInterceptor.class)
                .addPackage(PackagePrivateInterceptedTest.class.getPackage());
    }

    @Inject
    AbstractPackagePrivateClass<Integer> bean;

    @Inject
    ActualImpl actualImpl;

    @Test
    public void testInvocationIntercepted() {
        SomeInterceptor.INVOCATION_COUNT.set(0);
        bean.implementedMethod();
        bean.implementedMethod("foo");
        bean.abstractMethod();
        bean.foo(2);
        assertEquals(4, SomeInterceptor.INVOCATION_COUNT.get());

        SomeInterceptor.INVOCATION_COUNT.set(0);
        actualImpl.implementedMethod();
        actualImpl.implementedMethod("foo");
        actualImpl.abstractMethod();
        actualImpl.foo(1);
        assertEquals(4, SomeInterceptor.INVOCATION_COUNT.get());
    }
}
