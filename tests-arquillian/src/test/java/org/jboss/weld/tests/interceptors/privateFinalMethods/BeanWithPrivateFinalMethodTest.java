/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.privateFinalMethods;

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
 * Tests that for classes which have private final methods an interceptor subclass can be created.
 * Such methods are then ignored for interception.
 *
 * The point of the test is not to see deployment exception, the actual test is just to ensure that interceptor
 * was enabled and does something.
 */
@RunWith(Arquillian.class)
public class BeanWithPrivateFinalMethodTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(BeanWithPrivateFinalMethodTest.class))
                .addPackage(BeanWithPrivateFinalMethodTest.class.getPackage());
    }

    @Inject
    BeanWithPrivateFinalMethod bean;

    @Test
    public void testSubclassGenerationIgnoresPrivateFinalMethod() {
        // test interception works, e.g. that subclass was created
        Assert.assertEquals(0, MyInterceptor.INTERCEPTOR_INVOKED);
        bean.protectedMethod();
        Assert.assertEquals(1, MyInterceptor.INTERCEPTOR_INVOKED);
    }

}
