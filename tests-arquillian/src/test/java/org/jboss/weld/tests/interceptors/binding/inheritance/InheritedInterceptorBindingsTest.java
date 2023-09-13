/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.tests.interceptors.binding.inheritance;

import static org.junit.Assert.assertEquals;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.interceptors.binding.inheritance.Interceptors.AlphaInterceptor;
import org.jboss.weld.tests.interceptors.binding.inheritance.Interceptors.BravoInterceptor;
import org.jboss.weld.tests.interceptors.binding.inheritance.Interceptors.CharlieInterceptor;
import org.jboss.weld.tests.interceptors.binding.inheritance.Interceptors.DeltaInterceptor;
import org.jboss.weld.tests.interceptors.binding.inheritance.Interceptors.EchoInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see CDI-2
 *
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 *
 */
@RunWith(Arquillian.class)
public class InheritedInterceptorBindingsTest {

    @Inject
    private Person person;

    @Inject
    private MexicanPerson mexicanPerson;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(InheritedInterceptorBindingsTest.class))
                .intercept(AlphaInterceptor.class, BravoInterceptor.class, CharlieInterceptor.class, DeltaInterceptor.class,
                        EchoInterceptor.class)
                .addPackage(InheritedInterceptorBindingsTest.class.getPackage());
    }

    @Test
    public void testOnlyTheOverridingInterceptorBindingActive() {
        assertEquals(28, person.getAge());
    }

    @Test
    public void testMultipleInheritedInterceptorBindingsAreOkUnlessTheyAreDifferent() {
        assertEquals(32, mexicanPerson.getAge());
    }
}
