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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VerifyValuesTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(VerifyValuesTest.class))
                .alternate(Alpha.class, BravoProducer.class, CharlieProducer.class).addPackage(Alpha.class.getPackage())
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
        verifyStereotypes(attributes, AlphaStereotype.class);
        verifyQualifierTypes(attributes, AlphaQualifier.class, Named.class, Any.class);
        verifyTypes(attributes, Object.class, Alpha.class);
    }

    @Test
    public void testProducerMethod() {
        BeanAttributes<Bravo> attributes = extension.getBravo();
        assertNotNull(attributes);
        // scope
        assertEquals(RequestScoped.class, attributes.getScope());
        // name
        verifyName(attributes, "createBravo");
        assertTrue(attributes.isAlternative());
        verifyStereotypes(attributes, AlphaStereotype.class);
        verifyQualifierTypes(attributes, BravoQualifier.class, Named.class, Any.class);
        verifyTypes(attributes, BravoInterface.class, Object.class);
    }

    @Test
    public void testProducerField() {
        BeanAttributes<Charlie> attributes = extension.getCharlie();
        assertNotNull(attributes);
        // scope
        assertEquals(ApplicationScoped.class, attributes.getScope());
        // name
        verifyName(attributes, "charlie");
        assertFalse(attributes.isAlternative());
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
