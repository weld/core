/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.exception;

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

@RunWith(Arquillian.class)
public class ExceptionThrownByDecoratedBeanTest {

    @Inject
    private Foo foo;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExceptionThrownByDecoratedBeanTest.class))
                .decorate(FooDecorator.class)
                .addPackage(ExceptionThrownByDecoratedBeanTest.class.getPackage());
    }

    @Test
    public void testCheckedExceptionPropagatedThroughDecoratorCall() {
        try {
            foo.throwCheckedException1();
            Assert.fail();
        } catch (CheckedException expected) {
        }
    }

    @Test
    public void testCheckedExceptionThrownByDecorator() {
        try {
            foo.throwCheckedException2();
            Assert.fail();
        } catch (CheckedException expected) {
        }
    }

    @Test
    public void testCheckedExceptionPropagated() {
        try {
            foo.throwCheckedException3();
            Assert.fail();
        } catch (CheckedException expected) {
        }
    }

    @Test
    public void testCheckedExceptionPropagatedThroughAbstractDecoratorMethod() {
        try {
            foo.throwCheckedException4();
            Assert.fail();
        } catch (CheckedException expected) {
        }
    }

    @Test
    public void testRuntimeExceptionPropagatedThroughDecoratorCall() {
        try {
            foo.throwRuntimeException1();
            Assert.fail();
        } catch (CustomRuntimeException expected) {
        }
    }

    @Test
    public void testRuntimeExceptionThrownByDecorator() {
        try {
            foo.throwRuntimeException2();
            Assert.fail();
        } catch (CustomRuntimeException expected) {
        }
    }

    @Test
    public void testRuntimeExceptionPropagated() {
        try {
            foo.throwRuntimeException3();
            Assert.fail();
        } catch (CustomRuntimeException expected) {
        }
    }

    @Test
    public void testRuntimeExceptionPropagatedThroughAbstractDecoratorMethod() {
        try {
            foo.throwRuntimeException4();
            Assert.fail();
        } catch (CustomRuntimeException expected) {
        }
    }
}
