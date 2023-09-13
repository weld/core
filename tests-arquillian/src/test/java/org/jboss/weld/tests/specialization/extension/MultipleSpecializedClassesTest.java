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
package org.jboss.weld.tests.specialization.extension;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that if an extensions registers multiple {@link AnnotatedType}s for a given class, the resulting beans are all
 * specialized if a specializing bean exists.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class MultipleSpecializedClassesTest {

    @Inject
    private Foo foo;

    @Inject
    @FooQualifier
    private Foo qualifierFoo;

    @Inject
    private Instance<Product> product;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(MultipleSpecializedClassesTest.class))
                .addPackage(MultipleSpecializedClassesTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ModifyingExtension.class);
    }

    @Test
    public void testSpecializingBeanSpecializesBothSuperclassBeans() {
        assertTrue(foo instanceof Bar);
        assertTrue(qualifierFoo instanceof Bar);
        assertTrue(product.isUnsatisfied());
    }
}
