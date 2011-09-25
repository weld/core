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

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.introspector.WeldAnnotation;
import org.jboss.weld.logging.messages.MetadataMessage;
import org.jboss.weld.resources.ClassTransformer;
import org.slf4j.cal10n.LocLogger;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import static org.jboss.weld.logging.Category.REFLECTION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_RETENTION;

/**
 * Abstract representation of an annotation model
 *
 * @author Pete Muir
 */
public abstract class AnnotationModel<T extends Annotation> {
    private static final LocLogger log = loggerFactory().getLogger(REFLECTION);

    // The underlying annotation
    private WeldAnnotation<T> annotatedAnnotation;
    // Is the data valid?
    protected boolean valid;

    /**
     * Constructor
     *
     * @param type The annotation type
     */
    public AnnotationModel(Class<T> type, ClassTransformer transformer) {
        this.annotatedAnnotation = transformer.loadAnnotation(type);
        init();
    }

    /**
     * Initializes the type and validates it
     */
    protected void init() {
        initType();
        initValid();
        check();
    }

    /**
     * Initializes the type
     */
    protected void initType() {
        if (!Annotation.class.isAssignableFrom(getRawType())) {
            throw new DefinitionException(MetadataMessage.META_ANNOTATION_ON_WRONG_TYPE, getMetaAnnotationTypes(), getRawType());
        }
    }

    /**
     * Validates the data for correct annotation
     */
    protected void initValid() {
        this.valid = false;
        for (Class<? extends Annotation> annotationType : getMetaAnnotationTypes()) {
            if (annotatedAnnotation.isAnnotationPresent(annotationType)) {
                this.valid = true;
            }
        }
    }

    protected void check() {
        if (valid && (!annotatedAnnotation.isAnnotationPresent(Retention.class) || annotatedAnnotation.isAnnotationPresent(Retention.class) && !annotatedAnnotation.getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME))) {
            this.valid = false;
            log.debug(MISSING_RETENTION, annotatedAnnotation);
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
    public WeldAnnotation<T> getAnnotatedAnnotation() {
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