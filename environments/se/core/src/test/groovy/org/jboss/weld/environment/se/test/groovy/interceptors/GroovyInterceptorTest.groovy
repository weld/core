/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.groovy.interceptors;

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ArchivePaths
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.weld.environment.se.test.interceptors.AggregatingInterceptor;
import org.jboss.weld.environment.se.test.interceptors.InterceptorsTest;
import org.jboss.weld.environment.se.test.interceptors.RecordingInterceptor;
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(Arquillian.class)
class GroovyInterceptorTest {

    @Inject
    private MyBean bean

    @Inject
    private MyAplicationScopedBean applicationScopedBean

    @Deployment
    static Archive getDeployment() {
        ShrinkWrap.create(BeanArchive.class).intercept(LoggingInterceptor.class)
                .addPackage(GroovyInterceptorTest.class.getPackage());
    }

    @Test
    void testInterceptedInvocationOnDependentBean() {
        LoggingInterceptor.reset()
        bean.hi()
        assertEquals true,LoggingInterceptor.intercepted
    }
    
    @Test
    void testNonInterceptedInvocationOnDependentBean() {
        LoggingInterceptor.reset()
        bean.hiProxy()
        assertEquals false,LoggingInterceptor.intercepted
    }

    @Test
    void testInterceptedInvocationOnApplicationScopedBean() {
        LoggingInterceptor.reset()
        applicationScopedBean.hi()
        assertEquals true,LoggingInterceptor.intercepted
    }
    
    @Test
    void testNonInterceptedInvocationOnApplicationScopedBean() {
        LoggingInterceptor.reset()
        applicationScopedBean.hiProxy()
        assertEquals false,LoggingInterceptor.intercepted
    }
}
