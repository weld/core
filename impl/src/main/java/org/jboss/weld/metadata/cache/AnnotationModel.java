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
package org.jboss.weld.metadata.cache;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.logging.ReflectionLogger;

/**
 * Abstract representation of an annotation model
 *
 * @author Pete Muir
 */
public abstract class AnnotationModel<T extends Annotation> {

    // The underlying annotation
    private final AnnotatedType<T> annotatedAnnotation;
    // Is the data valid?
    protected boolean valid;

    /**
     * Constructor
     *
     * @param type The annotation type
     */
    public AnnotationModel(EnhancedAnnotation<T> enhancedAnnotatedAnnotation) {
        this.annotatedAnnotation = enhancedAnnotatedAnnotation.slim();
        init(enhancedAnnotatedAnnotation);
    }

    /**
     * Initializes the type and validates it
     */
    protected void init(EnhancedAnnotation<T> annotatedAnnotation) {
        initType(annotatedAnnotation);
        initValid(annotatedAnnotation);
        check(annotatedAnnotation);
    }

    /**
     * Initializes the type
     */
    protected void initType(EnhancedAnnotation<T> annotatedAnnotation) {
        if (!Annotation.class.isAssignableFrom(getRawType())) {
            throw MetadataLogger.LOG.metaAnnotationOnWrongType(getMetaAnnotationTypes(), getRawType());
        }
    }

    /**
     * Validates the data for correct annotation
     */
    protected void initValid(EnhancedAnnotation<T> annotatedAnnotation) {
        this.valid = false;
        for (Class<? extends Annotation> annotationType : getMetaAnnotationTypes()) {
            if (annotatedAnnotation.isAnnotationPresent(annotationType)) {
                this.valid = true;
            }
        }
    }

    protected void check(EnhancedAnnotation<T> annotatedAnnotation) {
        if (valid && (!annotatedAnnotation.isAnnotationPresent(Retention.class)
                || annotatedAnnotation.isAnnotationPresent(Retention.class)
                        && !annotatedAnnotation.getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME))) {
            this.valid = false;
            ReflectionLogger.LOG.missingRetention(annotatedAnnotation);
        }
    }

    /**
     * Gets the type of the annotation
     *
     * @return The type
     */
    public Class<T> getRawType() {
        return annotatedAnnotation.getJavaClass();
    }

    /**
     * Gets the meta-annotation that should be present
     *
     * @return
     */
    protected abstract Set<Class<? extends Annotation>> getMetaAnnotationTypes();

    /**
     * Indicates if the annotation is valid
     *
     * @return True if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the annotated annotation
     *
     * @return The annotation
     */
    public AnnotatedType<T> getAnnotatedAnnotation() {
        return annotatedAnnotation;
    }

    /**
     * Gets a string representation of the annotation model
     *
     * @return The string representation
     */
    @Override
    public String toString() {
        return (isValid() ? "Valid" : "Invalid") + " annotation model for " + getRawType();
    }

}
