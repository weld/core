/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.resolution.circular.self.modifiers;

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests self-interception when invoking method via client proxy.
 * Tests are for public/protected method and public/protected return type and parameter all of which should work.
 * No other modifiers are working for self-interception at the time of writing.
 */
@RunWith(Arquillian.class)
public class SelfInterceptionComplexTest {

    @Inject
    SomeInterface selfInjectingImpl;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SelfInterceptionComplexTest.class))
                .addPackage(SelfInterceptionComplexTest.class.getPackage());
    }

    @Test
    public void selfInvokePublicMethodPublicParamsPublicReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokePublicMethodPublicParamsPublicReturnType(10);
        Assert.assertEquals("_selfInvokePublicMethodPublicParamsPublicReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokePublicMethodProtectedParamsPublicReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokePublicMethodProtectedParamsPublicReturnType(10);
        Assert.assertEquals("_selfInvokePublicMethodProtectedParamsPublicReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokePublicMethodPublicParamsProtectedReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokePublicMethodPublicParamsProtectedReturnType(10);
        Assert.assertEquals("_selfInvokePublicMethodPublicParamsProtectedReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokePublicMethodProtectedParamsProtectedReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokePublicMethodProtectedParamsProtectedReturnType(10);
        Assert.assertEquals("_selfInvokePublicMethodProtectedParamsProtectedReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokeProtectedMethodProtectedParamsProtectedReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokeProtectedMethodProtectedParamsProtectedReturnType(10);
        Assert.assertEquals("_selfInvokeProtectedMethodProtectedParamsProtectedReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokeProtectedMethodPublicParamsProtectedReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokeProtectedMethodPublicParamsProtectedReturnType(10);
        Assert.assertEquals("_selfInvokeProtectedMethodPublicParamsProtectedReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokeProtectedMethodProtectedParamsPublicReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokeProtectedMethodProtectedParamsPublicReturnType(10);
        Assert.assertEquals("_selfInvokeProtectedMethodProtectedParamsPublicReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }

    @Test
    public void selfInvokeProtectedMethodPublicParamsPublicReturnTypeTest() {
        SelfInterceptor.nullifyCache();
        Assert.assertTrue(SelfInterceptor.INTERCEPTED_METHOD_NAME == null);
        selfInjectingImpl.selfInvokeProtectedMethodPublicParamsPublicReturnType(10);
        Assert.assertEquals("_selfInvokeProtectedMethodPublicParamsPublicReturnType", SelfInterceptor.INTERCEPTED_METHOD_NAME);
    }
}
