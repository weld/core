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
package org.jboss.weld.tests.interceptors.exclusion;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.Instance;
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
public class ExcludeClassInterceptorsTest {

    @Inject
    private Instance<InterceptedBean> bean;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExcludeClassInterceptorsTest.class))
                .addPackage(ExcludeClassInterceptorsTest.class.getPackage()).addClass(ActionSequence.class);
    }

    @Test
    public void testMethodInterceptors() {
        InterceptedBean instance = bean.get();
        ActionSequence.reset();
        instance.ping();
        assertEquals(1, ActionSequence.getSequenceData().size());
        assertEquals(Interceptor2.class.getName(), ActionSequence.getSequenceData().get(0));
    }

    @Test
    public void testConstructorInterceptors() {
        ActionSequence.reset();
        bean.get();
        assertEquals(1, ActionSequence.getSequenceData().size());
        assertEquals(Interceptor2.class.getName(), ActionSequence.getSequenceData().get(0));
    }
}
