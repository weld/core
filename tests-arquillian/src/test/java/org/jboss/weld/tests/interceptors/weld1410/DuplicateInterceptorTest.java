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
package org.jboss.weld.tests.interceptors.weld1410;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DuplicateInterceptorTest {

    @Inject
    private SimpleBean bean;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DuplicateInterceptorTest.class))
                .addPackage(DuplicateInterceptorTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Test
    public void testInvocation() {

        List<String> expected = new ArrayList<String>();
        expected.add(0 + Interceptor1.class.getSimpleName());
        expected.add(0 + Interceptor2.class.getSimpleName());
        expected.add(1 + Interceptor1.class.getSimpleName());
        expected.add(1 + Interceptor2.class.getSimpleName());
        expected.add(2 + Interceptor1.class.getSimpleName());

        ActionSequence.reset();
        bean.ping();
        assertEquals(expected, ActionSequence.getSequenceData());
    }
}
