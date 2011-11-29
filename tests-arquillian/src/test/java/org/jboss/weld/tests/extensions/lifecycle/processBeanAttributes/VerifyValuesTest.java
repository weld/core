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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VerifyValuesTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).alternate(Alpha.class, BravoProducer.class, CharlieProducer.class).addPackage(Alpha.class.getPackage())
                .addClass(BeanUtilities.class).addAsServiceProvider(Extension.class, VerifyingExtension.class);
    }

    @Test
    public void testClassLevelBeanAttributes() {
        BeanAttributes<Alpha> attributes = extension.getAlpha();
        assertNotNull(attributes);
        // scope
        assertEquals(ApplicationScoped.class, attributes.getScope());
        // name
        verifyName(attributes, "alpha");
        assertTrue(attributes.isAlternative());
        assertTrue(attributes.isNullable());
        verifyStereotypes(attributes, AlphaStereotype.class);
        verifyQualifierTypes(attributes, AlphaQualifier.class, Named.class, Any.class);
        verifyTypes(attributes, Object.class, Alpha.class);
    }

    @Test
    public void testProducerMethod() {
        BeanAttributes<BravoProducer> attributes = extension.getBravo();
        assertNotNull(attributes);
        // scope
        assertEquals(RequestScoped.class, attributes.getScope());
        // name
        verifyName(attributes, "createBravo");
        assertTrue(attributes.isAlternative()); // because alpha is
        assertTrue(attributes.isNullable());
        verifyStereotypes(attributes, AlphaStereotype.class);
        verifyQualifierTypes(attributes, BravoQualifier.class, Named.class, Any.class);
        verifyTypes(attributes, BravoInterface.class, Object.class);
    }

    @Test
    public void testProducerField() {
        BeanAttributes<CharlieProducer> attributes = extension.getCharlie();
        assertNotNull(attributes);
        // scope
        assertEquals(ApplicationScoped.class, attributes.getScope());
        // name
        verifyName(attributes, "charlie");
        assertTrue(attributes.isAlternative()); // because alpha is
        assertTrue(attributes.isNullable());
        verifyStereotypes(attributes, AlphaStereotype.class);
        verifyQualifierTypes(attributes, CharlieQualifier.class, Named.class, Any.class);
        verifyTypes(attributes, Object.class, Charlie.class, CharlieInterface.class);
    }

    @Test
    public void testEventNotFiredForNonEnabledBean() {
        assertNull(extension.getMike());
    }

    private void verifyName(BeanAttributes<?> attributes, String name) {
        assertEquals(name, attributes.getName());
        for (Annotation qualifier : attributes.getQualifiers()) {
            if (Named.class.equals(qualifier.annotationType())) {
                assertEquals(name, ((Named) qualifier).value());
                return;
            }
        }
        fail("@Named qualifier not found.");
    }
}
