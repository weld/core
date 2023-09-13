/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.annotatedType.withAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.EmbeddedContainer;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Jozef Hartinger
 *
 * @see https://issues.jboss.org/browse/WFLY-1573
 *
 */
@RunWith(Arquillian.class)
public class WithAnnotationsTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(WithAnnotationsTest.class))
                .addPackage(WithAnnotationsTest.class.getPackage())
                .addAsServiceProvider(Extension.class, VerifyingExtension.class);
    }

    @Test
    public void test() {
        assertNotNull(extension.getPersonType());
        assertEquals(Person.class, extension.getPersonType().getJavaClass());

        assertNotNull(extension.getGroupType());
        assertEquals(Group.class, extension.getGroupType().getJavaClass());
    }

    // We need to update WildFly ClassFileInfo first
    @Category(EmbeddedContainer.class)
    @Test
    public void testWithAnnotationsOnDefaultMethod() {
        assertNotNull(extension.getMyBeanType());
        assertEquals(MyBean.class, extension.getMyBeanType().getJavaClass());

        assertNotNull(extension.getMyBeanMetaType());
        assertEquals(MyBeanMeta.class, extension.getMyBeanMetaType().getJavaClass());
    }
}
