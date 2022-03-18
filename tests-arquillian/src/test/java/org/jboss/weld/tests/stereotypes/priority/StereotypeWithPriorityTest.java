/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.stereotypes.priority;

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
public class StereotypeWithPriorityTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(StereotypeWithPriorityTest.class))
                .addPackage(StereotypeWithPriorityTest.class.getPackage());
    }

    @Inject
    Foo foo;

    @Inject
    Bar bar;

    @Inject
    Baz baz;

    @Inject
    Charlie charlie;

    @Test
    public void testStereotypeWithPriority() {
        // injected Foo should be FooAlternative
        Assert.assertEquals(FooAlternative.class.getSimpleName(), foo.ping());
    }

    @Test
    public void testStereotypeWithAlternativeAndPriority() {
        // injected Bar should be instance of BarExtended
        Assert.assertEquals(BarExtended.class.getSimpleName(), bar.ping());
    }

    @Test
    public void testBeanPriorityFromStereotypeOverridesOtherAlternative() {
        // injected Baz should be instance of BazAlternative2
        Assert.assertEquals(BazAlternative2.class.getSimpleName(), baz.ping());
    }

    @Test
    public void testBeanOverridesPriorityFromStereotype() {
        // injected Charlie should be instance of CharlieAlternative
        Assert.assertEquals(CharlieAlternative.class.getSimpleName(), charlie.ping());
    }
}
