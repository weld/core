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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifiers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanAttributes;
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
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SpecializationTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SpecializationTest.class))
                .addClasses(Foo.class, Bar.class, Baz.class, Alpha.class, Bravo.class, Charlie.class, VerifyingExtension.class)
                .addClass(BeanUtilities.class).addAsServiceProvider(Extension.class, VerifyingExtension.class);
    }

    @Test
    public void testProcessBeanAttributesFiredProperlyForSpecializedBean(BeanManager manager) {
        assertNull(extension.getAlpha());
        assertNull(extension.getBravo());
        validateCharlie(extension.getCharlie());
        validateCharlie(manager.resolve(manager.getBeans(Alpha.class, Any.Literal.INSTANCE)));
    }

    private void validateCharlie(BeanAttributes<?> attributes) {
        verifyQualifiers(attributes, Foo.Literal.INSTANCE, Bar.Literal.INSTANCE, Baz.Literal.INSTANCE, Any.Literal.INSTANCE,
                new NamedLiteral("alpha"));
        assertEquals("alpha", attributes.getName());
    }
}
