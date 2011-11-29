/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifiers;
import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SpecializationTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addClasses(Foo.class, Bar.class, Baz.class, Alpha.class, Bravo.class, Charlie.class, VerifyingExtension.class)
                .addClass(BeanUtilities.class).addAsServiceProvider(Extension.class, VerifyingExtension.class);
    }

    @Test
    public void testProcessBeanAttributesFiredProperlyForSpecializedBean(BeanManager manager) {
        validateBravo(extension.getBravo());
        validateCharlie(extension.getCharlie());
        validateCharlie(manager.resolve(manager.getBeans(Alpha.class, AnyLiteral.INSTANCE)));
    }

    private void validateBravo(BeanAttributes<Bravo> attributes) {

        verifyQualifiers(attributes, Foo.Literal.INSTANCE, Bar.Literal.INSTANCE, AnyLiteral.INSTANCE, new NamedLiteral("alpha"));
        assertEquals("alpha", attributes.getName());
    }

    private void validateCharlie(BeanAttributes<?> attributes) {
        verifyQualifiers(attributes, Foo.Literal.INSTANCE, Bar.Literal.INSTANCE, Baz.Literal.INSTANCE, AnyLiteral.INSTANCE, new NamedLiteral("alpha"));
        assertEquals("alpha", attributes.getName());
    }
}
