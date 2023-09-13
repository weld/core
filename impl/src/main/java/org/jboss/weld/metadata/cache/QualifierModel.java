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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.inject.Qualifier;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.security.SetAccessibleAction;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Model of a binding type
 *
 * @author Pete Muir
 */
public class QualifierModel<T extends Annotation> extends AbstractBindingModel<T> {

    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Collections
            .<Class<? extends Annotation>> singleton(Qualifier.class);

    /**
     * Constructor
     *
     * @param type The type
     */
    public QualifierModel(EnhancedAnnotation<T> enhancedAnnotatedAnnotation) {
        super(enhancedAnnotatedAnnotation);
    }

    @Override
    protected void initValid(EnhancedAnnotation<T> annotatedAnnotation) {
        super.initValid(annotatedAnnotation);
        // The annotation either has @Qualifier or was registered via extension
        // Only check annotation method in case the annotation itself is valid
        if (isValid()) {
            for (EnhancedAnnotatedMethod<?, ?> annotatedMethod : annotatedAnnotation.getMembers()) {
                if ((Reflections.isArrayType(annotatedMethod.getJavaClass())
                        || Annotation.class.isAssignableFrom(annotatedMethod.getJavaClass()))
                        && !getNonBindingMembers().contains(annotatedMethod.slim())) {
                    MetadataLogger.LOG.nonBindingMemberType(annotatedMethod);
                    super.valid = false;
                }
            }
        }
    }

    /**
     * Validates the members
     */
    protected void check(EnhancedAnnotation<T> annotatedAnnotation) {
        super.check(annotatedAnnotation);
        if (isValid()) {

            if (!annotatedAnnotation.isAnnotationPresent(Target.class)) {
                ReflectionLogger.LOG.missingTarget(annotatedAnnotation);
            } else if (!Arrays2.unorderedEquals(annotatedAnnotation.getAnnotation(Target.class).value(), METHOD, FIELD,
                    PARAMETER, TYPE)) {
                ReflectionLogger.LOG.missingTargetMethodFieldParameterType(annotatedAnnotation);
            }
        }
    }

    /**
     * Gets the meta-annotation type
     *
     * @return The BindingType class
     */
    @Override
    protected Set<Class<? extends Annotation>> getMetaAnnotationTypes() {
        return META_ANNOTATIONS;
    }

    /**
     * Indicates if there are non-binding types present
     *
     * @return True if present, false otherwise
     */
    public boolean hasNonBindingMembers() {
        return getNonBindingMembers().size() > 0;
    }

    /**
     * Comparator for checking equality
     *
     * @param instance The instance to check against
     * @param other The other binding type
     * @return True if equal, false otherwise
     */
    public boolean isEqual(Annotation instance, Annotation other) {
        if (instance.annotationType().equals(getRawType()) && other.annotationType().equals(getRawType())) {
            for (AnnotatedMethod<?> annotatedMethod : getAnnotatedAnnotation().getMethods()) {
                if (!getNonBindingMembers().contains(annotatedMethod)) {
                    try {
                        AccessController.doPrivileged(SetAccessibleAction.of(annotatedMethod.getJavaMember()));
                        Object thisValue = annotatedMethod.getJavaMember().invoke(instance);
                        Object thatValue = annotatedMethod.getJavaMember().invoke(other);
                        if (!thisValue.equals(thatValue)) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        throw new WeldException(e);
                    } catch (IllegalAccessException e) {
                        throw new WeldException(e);
                    } catch (InvocationTargetException e) {
                        throw new WeldException(e);
                    }

                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a string representation of the qualifier model
     *
     * @return The string representation
     */
    @Override
    public String toString() {
        return (isValid() ? "Valid" : "Invalid") + " qualifier model for " + getRawType() + " with non-binding members "
                + getNonBindingMembers();
    }

}
