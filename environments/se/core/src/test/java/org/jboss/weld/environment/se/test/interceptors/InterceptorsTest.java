/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.interceptors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Peter Royle
 */
@RunWith(Arquillian.class)
public class InterceptorsTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).intercept(AggregatingInterceptor.class, RecordingInterceptor.class)
                .addPackage(InterceptorsTest.class.getPackage());
    }

    /**
     * Test that interceptors work as expected in SE.
     */
    @Test
    public void testInterceptors(InterceptorTestBean intTestBean) {

        assertNotNull(intTestBean);

        intTestBean.doSomethingRecorded();
        assertTrue(RecordingInterceptor.methodsRecorded.contains("doSomethingRecorded"));

        intTestBean.doSomethingRecordedAndAggregated();

        assertEquals(1, AggregatingInterceptor.methodsCalled);
    }

}
