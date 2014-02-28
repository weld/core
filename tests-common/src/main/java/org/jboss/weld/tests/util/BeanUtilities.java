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

    private static final String FOUND_ON = " found on ";
    private static final String NOT_FOUND_ON = " not found on ";

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
        assertTrue("The following unexpected types " + types + FOUND_ON + attributes, types.isEmpty());
    }

    /**
     * Verifies that the set of stereotypes of a given {@link BeanAttributes} matches the expected stereotypes.
     */
    public static void verifyStereotypes(BeanAttributes<?> attributes, Class<?>... expected) {
        Set<Class<? extends Annotation>> stereotypes = new HashSet<Class<? extends Annotation>>(attributes.getStereotypes());
        assertEquals(expected.length, stereotypes.size());
        for (Class<?> stereotype : expected) {
            if (!stereotypes.remove(stereotype)) {
                fail("Expected stereotype " + stereotype + NOT_FOUND_ON + attributes);
            }
        }
        assertTrue("The following unexpected stereotypes " + stereotypes + FOUND_ON + attributes, stereotypes.isEmpty());
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
        assertTrue("Expected qualifiers" + expectedQualifiers + NOT_FOUND_ON + attributes, expectedQualifiers.isEmpty());
    }
}
