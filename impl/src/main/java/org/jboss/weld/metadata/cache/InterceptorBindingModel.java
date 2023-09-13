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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.interceptor.InterceptorBinding;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.security.SetAccessibleAction;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author Marius Bogoevici
 */
public class InterceptorBindingModel<T extends Annotation> extends AbstractBindingModel<T> {
    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Collections
            .<Class<? extends Annotation>> singleton(InterceptorBinding.class);
    private Set<Annotation> inheritedInterceptionBindingTypes;
    private Set<Annotation> metaAnnotations;

    public InterceptorBindingModel(EnhancedAnnotation<T> enhancedAnnotatedAnnotation) {
        super(enhancedAnnotatedAnnotation);
    }

    @Override
    protected void init(EnhancedAnnotation<T> annotatedAnnotation) {
        super.init(annotatedAnnotation);
        if (isValid()) {
            initInterceptionBindingTypes(annotatedAnnotation);
            checkArrayAndAnnotationValuedMembers(annotatedAnnotation);
            checkMetaAnnotations(annotatedAnnotation);
            this.metaAnnotations = annotatedAnnotation.getAnnotations();
        }
    }

    @Override
    protected Set<Class<? extends Annotation>> getMetaAnnotationTypes() {
        return META_ANNOTATIONS;
    }

    public Set<Annotation> getMetaAnnotations() {
        return metaAnnotations;
    }

    protected void initInterceptionBindingTypes(EnhancedAnnotation<T> annotatedAnnotation) {
        inheritedInterceptionBindingTypes = annotatedAnnotation.getMetaAnnotations(InterceptorBinding.class);
    }

    protected void check(EnhancedAnnotation<T> annotatedAnnotation) {
        super.check(annotatedAnnotation);
        if (isValid()) {
            if (!annotatedAnnotation.isAnnotationPresent(Target.class)) {
                ReflectionLogger.LOG.missingTarget(annotatedAnnotation);
            }
            if (!isValidTargetType(annotatedAnnotation)) {
                ReflectionLogger.LOG.missingTargetTypeMethodOrTargetType(annotatedAnnotation);
            }
        }
    }

    private static boolean isValidTargetType(EnhancedAnnotation<?> annotation) {
        Target target = annotation.getAnnotation(Target.class);
        return target != null
                && (Arrays2.unorderedEquals(target.value(), ElementType.TYPE, ElementType.METHOD)
                        || Arrays2.unorderedEquals(target.value(), ElementType.TYPE));
    }

    private void checkMetaAnnotations(EnhancedAnnotation<T> annotatedAnnotation) {
        ElementType[] elementTypes = getTargetElementTypes(annotatedAnnotation.getAnnotation(Target.class));
        for (Annotation inheritedBinding : getInheritedInterceptionBindingTypes()) {
            ElementType[] metaAnnotationElementTypes = getTargetElementTypes(
                    inheritedBinding.annotationType().getAnnotation(Target.class));
            if (!Arrays2.containsAll(metaAnnotationElementTypes, elementTypes)) {
                ReflectionLogger.LOG.invalidInterceptorBindingTargetDeclaration(inheritedBinding.annotationType().getName(),
                        Arrays.toString(metaAnnotationElementTypes), annotatedAnnotation.getJavaClass().getName(),
                        Arrays.toString(elementTypes));
            }
        }
    }

    private ElementType[] getTargetElementTypes(Target target) {
        if (target == null) {
            return ElementType.values();
        }
        return target.value();
    }

    private void checkArrayAndAnnotationValuedMembers(EnhancedAnnotation<T> annotatedAnnotation) {
        for (EnhancedAnnotatedMethod<?, ?> annotatedMethod : annotatedAnnotation.getMembers()) {
            if ((Reflections.isArrayType(annotatedMethod.getJavaClass())
                    || Annotation.class.isAssignableFrom(annotatedMethod.getJavaClass()))
                    && !getNonBindingMembers().contains(annotatedMethod.slim())) {
                throw MetadataLogger.LOG.nonBindingMemberTypeException(annotatedMethod);
            }
        }
    }

    /**
     * Retrieves the transitive interceptor binding types that are inherited by this interceptor binding, as per section 9.1.1
     * of the specification,
     * "Interceptor binding types with additional interceptor bindings"
     *
     * @return a set of transitive interceptor bindings, if any
     */
    public Set<Annotation> getInheritedInterceptionBindingTypes() {
        return inheritedInterceptionBindingTypes;
    }

    public boolean isEqual(Annotation instance, Annotation other) {
        return isEqual(instance, other, false);
    }

    public boolean isEqual(Annotation instance, Annotation other, boolean includeNonBindingTypes) {
        if (instance.annotationType().equals(getRawType()) && other.annotationType().equals(getRawType())) {
            for (AnnotatedMethod<?> annotatedMethod : getAnnotatedAnnotation().getMethods()) {
                if (includeNonBindingTypes || !getNonBindingMembers().contains(annotatedMethod)) {
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
}
