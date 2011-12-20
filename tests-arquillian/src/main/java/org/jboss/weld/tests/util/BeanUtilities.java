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
package org.jboss.weld.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;

/**
 * Utility methods for testing {@link BeanAttributes} and {@link Bean}s.
 *
 * @author Jozef Hartinger
 *
 */
public class BeanUtilities {

    private BeanUtilities() {
    }

    /**
     * Verifies that the set of bean types of a given {@link BeanAttributes} matches the expected types.
     */
    public static void verifyTypes(BeanAttributes<?> attributes, Type... expectedTypes) {
        Set<Type> types = new HashSet<Type>(attributes.getTypes());
        for (Type type : expectedTypes) {
            if (!types.remove(type)) {
                fail("Expected type " + type + " not a bean type of " + attributes);
            }
        }
        assertTrue("The following unexpected types " + types + " found on " + attributes, types.isEmpty());
    }

    /**
     * Verifies that the set of stereotypes of a given {@link BeanAttributes} matches the expected stereotypes.
     */
    public static void verifyStereotypes(BeanAttributes<?> attributes, Class<?>... expected) {
        Set<Class<? extends Annotation>> stereotypes = new HashSet<Class<? extends Annotation>>(attributes.getStereotypes());
        assertEquals(expected.length, stereotypes.size());
        for (Class<?> stereotype : expected) {
            if (!stereotypes.remove(stereotype)) {
                fail("Expected stereotype " + stereotype + " not found on " + attributes);
            }
        }
        assertTrue("The following unexpected stereotypes " + stereotypes + " found on " + attributes, stereotypes.isEmpty());
    }

    /**
     * Verifies that the set of qualifiers of a given {@link BeanAttributes} matches the given set of annotation types.
     */
    public static void verifyQualifierTypes(BeanAttributes<?> attributes, Class<?>... expectedTypes) {
        verifyQualifierTypes(attributes.getQualifiers(), expectedTypes);
    }

    public static void verifyQualifierTypes(Set<Annotation> annotations, Class<?>... expectedTypes) {
        Set<Class<?>> expectedQualifierTypes = new HashSet<Class<?>>(Arrays.asList(expectedTypes));
        for (Annotation qualifier : annotations) {
            if (!expectedQualifierTypes.remove(qualifier.annotationType())) {
                fail("Unexpected qualifier type " + qualifier.annotationType());
            }
        }
        assertTrue("Expected qualifier types " + expectedQualifierTypes + " not found", expectedQualifierTypes.isEmpty());
    }

    /**
     * Verifies that the set of qualifiers of a given {@link BeanAttributes} matches the given set of qualifiers.
     */
    public static void verifyQualifiers(BeanAttributes<?> attributes, Annotation... expectedAnnotations) {
        Set<Annotation> expectedQualifiers = new HashSet<Annotation>(Arrays.asList(expectedAnnotations));
        for (Annotation qualifier : attributes.getQualifiers()) {
            if (!expectedQualifiers.remove(qualifier)) {
                fail("Expected qualifier not present " + qualifier.annotationType());
            }
        }
        assertTrue("Expected qualifiers" + expectedQualifiers + " not found on " + attributes, expectedQualifiers.isEmpty());
    }
}
