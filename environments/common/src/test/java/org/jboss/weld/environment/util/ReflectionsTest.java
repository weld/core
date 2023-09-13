/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.util;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.AnnotationLiteral;

import org.jboss.weld.literal.NamedLiteral;
import org.junit.Test;

public class ReflectionsTest {

    @Test
    public void testHasMetaAnnotationSpecified() {
        assertFalse(Reflections.hasBeanDefiningMetaAnnotationSpecified(new Annotation[] {}, NormalScope.class));
        assertTrue(Reflections.hasBeanDefiningMetaAnnotationSpecified(
                new Annotation[] { NamedLiteral.DEFAULT, FooStereotype.LITERAL }, Stereotype.class));
        assertFalse(Reflections.hasBeanDefiningMetaAnnotationSpecified(
                new Annotation[] { NamedLiteral.DEFAULT, FooStereotype.LITERAL }, NormalScope.class));
        assertTrue(Reflections.hasBeanDefiningMetaAnnotationSpecified(
                new Annotation[] { NamedLiteral.DEFAULT, FooNormalScoped.LITERAL }, NormalScope.class));
    }

    @Stereotype
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface FooStereotype {

        @SuppressWarnings("serial")
        AnnotationLiteral<FooStereotype> LITERAL = new AnnotationLiteral<FooStereotype>() {
        };

    }

    @NormalScope(passivating = true)
    @Inherited
    @Target({ TYPE })
    @Retention(RUNTIME)
    private static @interface FooNormalScoped {

        @SuppressWarnings("serial")
        AnnotationLiteral<FooNormalScoped> LITERAL = new AnnotationLiteral<FooNormalScoped>() {
        };

    }

}
