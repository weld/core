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
package org.jboss.weld.tests.extensions.annotatedType.ejb;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests that it is possible to override ejb annotations through the SPI
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class AnnotatedTypeSessionBeanTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AnnotatedTypeSessionBeanTest.class))
                .addPackage(AnnotatedTypeSessionBeanTest.class.getPackage())
                .addPackage(TestAnnotatedTypeBuilder.class.getPackage())
                .addAsServiceProvider(Extension.class, AnnotatedTypeEjbExtension.class);
    }

    @Test
    public void testOverridingEjbAnnotations(@ConveyorShaft Shaft conveyerShaft) {
        Assert.assertNotNull(conveyerShaft);
    }

    @Test
    public void testAddingBultipleBeansPerEjbClass(@BigLathe LatheLocal bigLathe, @SmallLathe LatheLocal smallLathe) {
        Assert.assertNotNull(bigLathe);
        Assert.assertNotNull(smallLathe);
    }
}
