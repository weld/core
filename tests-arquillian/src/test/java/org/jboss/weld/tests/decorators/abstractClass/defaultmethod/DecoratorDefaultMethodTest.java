/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.decorators.abstractClass.defaultmethod;

import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The hierarchy has to include interface with default method, abstract class and finally implementing class which does *not*
 * override the default method.
 * Decorator has to only override the default method and nothing else.
 *
 * @see WELD-2501
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class DecoratorDefaultMethodTest {

    @Inject
    @Any
    private BeanInterface bean;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DecoratorDefaultMethodTest.class))
                .addPackage(DecoratorDefaultMethodTest.class.getPackage());
    }

    /**
     * Tests whether a default method in a decorated interface that is not overridden in any subclass of the interface (except
     * for the decorator)
     * gets decorated when called.
     */
    @Test
    public void testDefaultMethodGetsIntercepted() {
        DecoratorOverridingDefaultMethod.reset();

        // make sure the interceptor works.
        bean.methodTwo("ape", 1);
        Assert.assertEquals("A method with default implementation in an interface should be decorated", 1,
                DecoratorOverridingDefaultMethod.decoratedInvocationCount);
        // make sure the method gets intercepted even when it is the *only* overridden method *and* has a default implementation.
        bean.methodOne("ape", "banana");
        Assert.assertEquals(
                "A method with default implementation in an interface should be decorated even when the bean does not override it and it is the *only* decorated method",
                2, DecoratorOverridingDefaultMethod.decoratedInvocationCount);
    }
}
