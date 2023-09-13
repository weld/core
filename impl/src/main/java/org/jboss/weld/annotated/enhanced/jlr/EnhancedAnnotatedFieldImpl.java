/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.annotated.enhanced.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import jakarta.enterprise.inject.spi.AnnotatedField;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Represents an annotated field
 * <p/>
 * This class is immutable, and therefore threadsafe
 *
 * @param <T>
 * @author Pete Muir
 */
public class EnhancedAnnotatedFieldImpl<T, X> extends AbstractEnhancedAnnotatedMember<T, X, Field>
        implements EnhancedAnnotatedField<T, X> {

    private final AnnotatedField<X> slim;

    public static <X, Y extends X> EnhancedAnnotatedFieldImpl<?, X> of(AnnotatedField<X> annotatedField,
            EnhancedAnnotatedType<Y> declaringClass, ClassTransformer classTransformer) {
        EnhancedAnnotatedType<X> downcastDeclaringType = Reflections.cast(declaringClass);
        return new EnhancedAnnotatedFieldImpl<Object, X>(annotatedField, buildAnnotationMap(annotatedField.getAnnotations()),
                buildAnnotationMap(annotatedField.getAnnotations()), downcastDeclaringType, classTransformer);
    }

    /**
     * Constructor
     * <p/>
     * Initializes the superclass with the built annotation map and detects the
     * type arguments
     *
     * @param field The actual field
     * @param declaringClass The abstraction of the declaring class
     */
    private EnhancedAnnotatedFieldImpl(AnnotatedField<X> annotatedField,
            Map<Class<? extends Annotation>, Annotation> annotationMap,
            Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, EnhancedAnnotatedType<X> declaringClass,
            ClassTransformer classTransformer) {
        super(annotatedField, annotationMap, declaredAnnotationMap, classTransformer, declaringClass);
        this.slim = annotatedField;
    }

    /**
     * Gets the underlying field
     *
     * @return The fields
     */
    public Field getAnnotatedField() {
        return slim.getJavaMember();
    }

    @Override
    public Field getDelegate() {
        return slim.getJavaMember();
    }

    /**
     * Gets the property name
     *
     * @return The property name
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField#getName()
     */
    public String getPropertyName() {
        return getName();
    }

    /**
     * Gets a string representation of the field
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotatedField(this);
    }

    public boolean isGeneric() {
        return false;
    }

    @Override
    public AnnotatedField<X> slim() {
        return slim;
    }
}
