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
package org.jboss.weld.tests.alternatives.customBeanPriority;

import jakarta.enterprise.inject.spi.Extension;
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
 * Tests that you can create a custom bean via (Weld)BeanConfigurator and give it a priority hence selecting it.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class CustomBeanPriorityTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(CustomBeanPriorityTest.class))
                .addClasses(CustomBeanPriorityTest.class, MyExtension.class, PlainFoo.class, FooAlternative.class)
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Inject
    PlainFoo foo;

    @Inject
    FooAlternative alternative;

    @Test
    public void contextLifecycleEventFiredForPostConstructCallbackActivation() {
        Assert.assertEquals("bar", alternative.ping());
        Assert.assertEquals("bar", foo.ping());
        Assert.assertEquals(1, MyExtension.PSB_OBSERVED);
    }
}
