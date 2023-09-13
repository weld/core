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
package org.jboss.weld.tests.beanManager.beanAttributes;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
public class CreateBeanAttributesTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(CreateBeanAttributesTest.class))
                .addPackage(Lake.class.getPackage()).addClass(BeanUtilities.class);
    }

    @Test
    public void testBeanAttributesForManagedBean() {
        AnnotatedType<Mountain> type = manager.createAnnotatedType(Mountain.class);
        BeanAttributes<Mountain> attributes = manager.createBeanAttributes(type);
        verifyTypes(attributes, Landmark.class, Object.class);
        verifyStereotypes(attributes, TundraStereotype.class);
        verifyQualifierTypes(attributes, Natural.class, Any.class);
        assertEquals(ApplicationScoped.class, attributes.getScope());
        assertEquals("mountain", attributes.getName());
        assertTrue(attributes.isAlternative());
    }

    @Test
    public void testBeanAttributesForManagedBeanWithModifiedAnnotatedType() {
        AnnotatedType<Mountain> type = manager.createAnnotatedType(Mountain.class);
        AnnotatedType<Mountain> wrappedType = new WrappedAnnotatedType<Mountain>(type, new NamedLiteral("Mount Blanc"));
        BeanAttributes<Mountain> attributes = manager.createBeanAttributes(wrappedType);
        verifyTypes(attributes, Mountain.class, Landmark.class, Object.class);
        assertTrue(attributes.getStereotypes().isEmpty());
        verifyQualifierTypes(attributes, Named.class, Any.class, Default.class);
        assertEquals(Dependent.class, attributes.getScope());
        assertEquals("Mount Blanc", attributes.getName());
        assertFalse(attributes.isAlternative());
    }

    @Test
    public void testBeanAttributesForSessionBean() {
        AnnotatedType<Lake> type = manager.createAnnotatedType(Lake.class);
        BeanAttributes<Lake> attributes = manager.createBeanAttributes(type);
        verifyTypes(attributes, LakeLocal.class, WaterBody.class, Landmark.class, Object.class);
        verifyStereotypes(attributes, TundraStereotype.class);
        verifyQualifierTypes(attributes, Natural.class, Any.class);
        assertEquals(Dependent.class, attributes.getScope());
        assertEquals("lake", attributes.getName());
        assertTrue(attributes.isAlternative());
    }

    @Test
    public void testBeanAttributesForMethod() {
        AnnotatedType<Dam> type = manager.createAnnotatedType(Dam.class);

        AnnotatedMethod<?> lakeFishMethod = null;
        AnnotatedMethod<?> damFishMethod = null;
        AnnotatedMethod<?> volumeMethod = null;

        for (AnnotatedMethod<?> method : type.getMethods()) {
            if (method.getJavaMember().getName().equals("getFish")
                    && method.getJavaMember().getDeclaringClass().equals(Dam.class)) {
                damFishMethod = method;
            }
            if (method.getJavaMember().getName().equals("getFish")
                    && method.getJavaMember().getDeclaringClass().equals(Lake.class)) {
                lakeFishMethod = method;
            }
            if (method.getJavaMember().getName().equals("getVolume")
                    && method.getJavaMember().getDeclaringClass().equals(Lake.class)) {
                volumeMethod = method;
            }
        }
        assertNotNull(lakeFishMethod);
        assertNotNull(damFishMethod);
        assertNotNull(volumeMethod);

        verifyLakeFish(manager.createBeanAttributes(lakeFishMethod));
        verifyDamFish(manager.createBeanAttributes(damFishMethod));
        verifyVolume(manager.createBeanAttributes(volumeMethod));
    }

    @Test
    public void testBeanAttributesForField() {
        AnnotatedType<Dam> type = manager.createAnnotatedType(Dam.class);

        AnnotatedField<?> lakeFishField = null;
        AnnotatedField<?> damFishField = null;
        AnnotatedField<?> volumeField = null;

        for (AnnotatedField<?> field : type.getFields()) {
            if (field.getJavaMember().getName().equals("fish") && field.getJavaMember().getDeclaringClass().equals(Dam.class)) {
                damFishField = field;
            }
            if (field.getJavaMember().getName().equals("fish")
                    && field.getJavaMember().getDeclaringClass().equals(Lake.class)) {
                lakeFishField = field;
            }
            if (field.getJavaMember().getName().equals("volume")
                    && field.getJavaMember().getDeclaringClass().equals(Lake.class)) {
                volumeField = field;
            }
        }
        assertNotNull(lakeFishField);
        assertNotNull(damFishField);
        assertNotNull(volumeField);

        verifyLakeFish(manager.createBeanAttributes(lakeFishField));
        verifyDamFish(manager.createBeanAttributes(damFishField));
        verifyVolume(manager.createBeanAttributes(volumeField));
    }

    private void verifyLakeFish(BeanAttributes<?> attributes) {
        verifyTypes(attributes, Fish.class, Object.class);
        verifyStereotypes(attributes, TundraStereotype.class);
        verifyQualifierTypes(attributes, Natural.class, Any.class, Named.class);
        assertEquals(ApplicationScoped.class, attributes.getScope());
        assertEquals("fish", attributes.getName());
        assertTrue(attributes.isAlternative());
    }

    private void verifyDamFish(BeanAttributes<?> attributes) {
        verifyTypes(attributes, Fish.class, Animal.class, Object.class);
        assertTrue(attributes.getStereotypes().isEmpty());
        verifyQualifierTypes(attributes, Wild.class, Any.class);
        assertEquals(Dependent.class, attributes.getScope());
        assertNull(attributes.getName());
        assertFalse(attributes.isAlternative());
    }

    private void verifyVolume(BeanAttributes<?> attributes) {
        verifyTypes(attributes, long.class, Object.class);
        assertTrue(attributes.getStereotypes().isEmpty());
        verifyQualifierTypes(attributes, Any.class, Default.class, Named.class);
        assertEquals(Dependent.class, attributes.getScope());
        assertEquals("volume", attributes.getName());
        assertFalse(attributes.isAlternative());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMember() {
        AnnotatedConstructor<?> constructor = manager.createAnnotatedType(WrappedAnnotatedType.class).getConstructors()
                .iterator().next();
        manager.createBeanAttributes(constructor);
    }
}
