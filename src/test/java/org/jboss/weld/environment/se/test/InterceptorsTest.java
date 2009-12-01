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
package org.jboss.weld.environment.se.test;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.Shutdown;
import org.jboss.weld.environment.se.test.beans.InterceptorTestBean;
import org.jboss.weld.environment.se.test.interceptors.AggregatingInterceptor;
import org.jboss.weld.environment.se.test.interceptors.RecordingInterceptor;
import org.jboss.weld.environment.se.util.WeldManagerUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Peter Royle
 */
public class InterceptorsTest
{

    /**
     * Test that interceptors work as expected in SE.
     */
    @Test
    public void testInterceptors()
    {
        WeldContainer weld = new Weld().initialize();
        BeanManager manager = weld.getBeanManager();

        InterceptorTestBean intTestBean = WeldManagerUtils.getInstanceByType(manager, InterceptorTestBean.class);
        Assert.assertNotNull(intTestBean);

        intTestBean.doSomethingRecorded();
        System.out.println(RecordingInterceptor.methodsRecorded);
        System.out.println(AggregatingInterceptor.methodsCalled);
        Assert.assertTrue(RecordingInterceptor.methodsRecorded.contains("doSomethingRecorded"));

        intTestBean.doSomethingRecordedAndAggregated();
        System.out.println(RecordingInterceptor.methodsRecorded);
        System.out.println(AggregatingInterceptor.methodsCalled);

        Assert.assertEquals(1, AggregatingInterceptor.methodsCalled);

        shutdownManager(manager);
    }

    private void shutdownManager(BeanManager manager)
    {
        manager.fireEvent(manager, new ShutdownAnnotation());
    }

    private static class ShutdownAnnotation extends AnnotationLiteral<Shutdown>
    {

        public ShutdownAnnotation()
        {
        }
    }
}
