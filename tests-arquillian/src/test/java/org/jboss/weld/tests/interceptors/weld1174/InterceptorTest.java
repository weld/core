/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.tests.interceptors.weld1174;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class InterceptorTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorTest.class))
                .intercept(Interceptor2.class, Interceptor3.class)
                .addPackage(InterceptorTest.class.getPackage());
    }

    @Inject
    InterceptedManagedBean interceptedManagedBean;

    @Inject
    InterceptedSessionBean interceptedSessionBean;

    @Before
    public void setUp() {
        VisitList.reset();
    }

    @Test
    public void testInterceptorOrderOnManagedBean() throws Exception {
        interceptedManagedBean.test();
        assertInterceptorsWereInvokedInCorrectOrder();
    }

    @Test
    @Category(Integration.class)
    public void testInterceptorOrderOnSessionBean() throws Exception {
        interceptedSessionBean.test();
        assertInterceptorsWereInvokedInCorrectOrder();
    }

    private void assertInterceptorsWereInvokedInCorrectOrder() {
        List<String> expectedOrder = Arrays.asList(
                Interceptor0.class.getSimpleName(),
                Interceptor1.class.getSimpleName(),
                Interceptor2.class.getSimpleName(),
                Interceptor3.class.getSimpleName());

        assertEquals(expectedOrder, VisitList.getList());
    }

}
