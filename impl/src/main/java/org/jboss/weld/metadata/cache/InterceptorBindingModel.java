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

import static org.jboss.weld.logging.Category.REFLECTION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.MetadataMessage.NON_BINDING_MEMBER_TYPE;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_TARGET;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_TARGET_TYPE_METHOD_OR_TARGET_TYPE;
import static org.jboss.weld.logging.messages.ReflectionMessage.TARGET_TYPE_METHOD_INHERITS_FROM_TARGET_TYPE;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * @author Marius Bogoevici
 */
public class InterceptorBindingModel<T extends Annotation> extends AbstractBindingModel<T> {
    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Collections.<Class<? extends Annotation>>singleton(InterceptorBinding.class);
    private static final LocLogger log = loggerFactory().getLogger(REFLECTION);
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
                log.debug(MISSING_TARGET, annotatedAnnotation);
            }
            if (!isValidTargetType(annotatedAnnotation)) {
                log.debug(MISSING_TARGET_TYPE_METHOD_OR_TARGET_TYPE, annotatedAnnotation);
            }
        }
    }

    private static boolean isValidTargetType(EnhancedAnnotation<?> annotation) {
        Target target = annotation.getAnnotation(Target.class);
        return target != null && (Arrays2.unorderedEquals(target.value(), ElementType.TYPE, ElementType.METHOD) || Arrays2.unorderedEquals(target.value(), ElementType.TYPE));
    }

    private void checkMetaAnnotations(EnhancedAnnotation<T> annotatedAnnotation) {
        Target target = annotatedAnnotation.getAnnotation(Target.class);
        if (target != null && Arrays2.containsAll(target.value(), ElementType.METHOD)) {
            for (Annotation inheritedBinding : getInheritedInterceptionBindingTypes()) {
                target = inheritedBinding.annotationType().getAnnotation(Target.class);
                if (target != null && !Arrays2.containsAll(target.value(), ElementType.METHOD)) {
                    log.debug(TARGET_TYPE_METHOD_INHERITS_FROM_TARGET_TYPE, annotatedAnnotation, inheritedBinding);
                }
            }
        }
    }

    private void checkArrayAndAnnotationValuedMembers(EnhancedAnnotation<T> annotatedAnnotation) {
        for (EnhancedAnnotatedMethod<?, ?> annotatedMethod : annotatedAnnotation.getMembers()) {
            if ((Reflections.isArrayType(annotatedMethod.getJavaClass()) || Annotation.class.isAssignableFrom(annotatedMethod.getJavaClass())) && !getNonBindingMembers().contains(annotatedMethod.slim())) {
                throw new DefinitionException(NON_BINDING_MEMBER_TYPE, annotatedMethod);
            }
        }
    }

    /**
     * Retrieves the transitive interceptor binding types that are inherited by
     * this interceptor binding, as per section 9.1.1 of the specification,
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
                        Object thisValue = RuntimeAnnotatedMembers.invokeMethod(annotatedMethod, instance);
                        Object thatValue = RuntimeAnnotatedMembers.invokeMethod(annotatedMethod, other);
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
