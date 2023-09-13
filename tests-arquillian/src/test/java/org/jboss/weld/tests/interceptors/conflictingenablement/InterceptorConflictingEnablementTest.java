/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.tests.interceptors.conflictingenablement;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1780
 */
@RunWith(Arquillian.class)
public class InterceptorConflictingEnablementTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorConflictingEnablementTest.class))
                .intercept(TransactionalInterceptor.class, LoggingInterceptor.class)
                .decorate(TestDecorator.class, AnotherTestDecorator.class)
                .addPackage(InterceptorConflictingEnablementTest.class.getPackage()).addClass(ActionSequence.class);
    }

    @Test
    public void testInterception(TestBean testBean) {
        ActionSequence.reset();
        testBean.ping();
        List<String> data = ActionSequence.getSequenceData();
        assertEquals(4, data.size());
        assertEquals(LoggingInterceptor.class.getName(), data.get(0));
        assertEquals(TransactionalInterceptor.class.getName(), data.get(1));
        assertEquals(AnotherTestDecorator.class.getName(), data.get(2));
        assertEquals(TestDecorator.class.getName(), data.get(3));
    }

}
