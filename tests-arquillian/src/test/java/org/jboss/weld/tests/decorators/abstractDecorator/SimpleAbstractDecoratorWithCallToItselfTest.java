/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.decorators.abstractDecorator;

import static org.jboss.weld.tests.decorators.abstractDecorator.AbstractDecoratorTestHelper.resetAll;

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
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
@RunWith(Arquillian.class)
public class SimpleAbstractDecoratorWithCallToItselfTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(SimpleAbstractDecoratorWithCallToItselfTest.class))
                .decorate(
                        FrameWithFieldInjectedDelegateAndSelfInvokedAbstractMethod.class,
                        FrameWithFieldInjectedDelegate.class)
                .addPackage(SimpleAbstractDecoratorWithCallToItselfTest.class.getPackage());
    }

    @Test
    public void testAbstractDecoratorApplied(WindowImpl window) {
        resetAll();
        window.move();
        Assert.assertTrue(WindowImpl.drawn);
        Assert.assertTrue(FrameWithFieldInjectedDelegate.drawn);
        Assert.assertTrue(FrameWithFieldInjectedDelegateAndSelfInvokedAbstractMethod.moved);
    }
}
