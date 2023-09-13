/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event.observer.vetoreplace;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExperimentalProcessObserverMethodTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExperimentalProcessObserverMethodTest.class))
                .addPackage(ExperimentalProcessObserverMethodTest.class.getPackage()).addAsServiceProvider(
                        Extension.class, DummyExtension.class);
    }

    @Test
    public void testVeto() {
        Assert.assertEquals(0, manager.resolveObserverMethods("foo", Experimental.Literal.INSTANCE).size());
    }

    @Test
    public void testReplacingObserverMethod() {
        @SuppressWarnings("serial")
        final Number number = new Number() {
            @Override
            public long longValue() {
                return 0L;
            }

            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public float floatValue() {
                return 0F;
            }

            @Override
            public double doubleValue() {
                return 0D;
            }
        };
        Assert.assertEquals(0, manager.resolveObserverMethods(number, Experimental.Literal.INSTANCE).size());
        Assert.assertEquals(0,
                manager.resolveObserverMethods(number, Experimental.Literal.INSTANCE, new NamedLiteral("experimental")).size());
        Assert.assertEquals(0, manager.resolveObserverMethods(0, Experimental.Literal.INSTANCE).size());
        Assert.assertEquals(1,
                manager.resolveObserverMethods(0, Experimental.Literal.INSTANCE, new NamedLiteral("experimental")).size());
    }
}
