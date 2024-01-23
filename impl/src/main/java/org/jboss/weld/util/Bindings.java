/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;

import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Utility methods for working with qualifiers and interceptor bindings.
 *
 * @author Jozef Hartinger
 *
 */
public class Bindings {

    public static final Set<Annotation> DEFAULT_QUALIFIERS = ImmutableSet.of(Any.Literal.INSTANCE, Default.Literal.INSTANCE);

    private Bindings() {
    }

    public static boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2, MetaAnnotationStore store) {
        checkQualifier(qualifier1, store);
        checkQualifier(qualifier2, store);

        QualifierInstance q1 = QualifierInstance.of(qualifier1, store);
        QualifierInstance q2 = QualifierInstance.of(qualifier2, store);
        return q1.equals(q2);
    }

    public static int getQualifierHashCode(Annotation qualifier, MetaAnnotationStore store) {
        checkQualifier(qualifier, store);
        return QualifierInstance.of(qualifier, store).hashCode();
    }

    private static void checkQualifier(Annotation qualifier, MetaAnnotationStore store) {
        Preconditions.checkNotNull(qualifier);

        QualifierModel<?> model = store.getBindingTypeModel(qualifier.annotationType());
        if (model == null || !model.isValid()) {
            throw BeanManagerLogger.LOG.invalidQualifier(qualifier);
        }
    }

    public static void validateQualifiers(Iterable<Annotation> qualifiers, BeanManager manager, Object definer,
            String nullErrorMessage) {
        if (qualifiers == null) {
            throw MetadataLogger.LOG.qualifiersNull(nullErrorMessage, definer);
        }
        for (Annotation annotation : qualifiers) {
            if (!manager.isQualifier(annotation.annotationType())) {
                throw MetadataLogger.LOG.notAQualifier(annotation.annotationType(), definer);
            }
        }
    }

    public static boolean areInterceptorBindingsEquivalent(Annotation qualifier1, Annotation qualifier2,
            MetaAnnotationStore store) {
        checkInterceptorBinding(qualifier1, store);
        checkInterceptorBinding(qualifier2, store);

        QualifierInstance q1 = QualifierInstance.of(qualifier1, store);
        QualifierInstance q2 = QualifierInstance.of(qualifier2, store);
        return q1.equals(q2);
    }

    public static int getInterceptorBindingHashCode(Annotation qualifier, MetaAnnotationStore store) {
        checkInterceptorBinding(qualifier, store);
        return QualifierInstance.of(qualifier, store).hashCode();
    }

    private static void checkInterceptorBinding(Annotation qualifier, MetaAnnotationStore store) {
        Preconditions.checkNotNull(qualifier);

        InterceptorBindingModel<?> model = store.getInterceptorBindingModel(qualifier.annotationType());
        if (model == null || !model.isValid()) {
            throw BeanManagerLogger.LOG.interceptorResolutionWithNonbindingType(qualifier);
        }
    }

    /**
     * Normalize set of qualifiers for a bean - automatically adds {@code @Any} and {@code Default} if needed.
     *
     * @param qualifiers input set of qualifiers, possibly missing built-in qualifiers
     * @return normalized set of bean qualifiers
     */
    public static Set<Annotation> normalizeBeanQualifiers(Set<Annotation> qualifiers) {
        if (qualifiers.isEmpty()) {
            return DEFAULT_QUALIFIERS;
        }
        Set<Annotation> normalized = new HashSet<Annotation>(qualifiers);
        normalized.remove(Any.Literal.INSTANCE);
        normalized.remove(Default.Literal.INSTANCE);
        if (normalized.isEmpty()) {
            normalized = DEFAULT_QUALIFIERS;
        } else {
            ImmutableSet.Builder<Annotation> builder = ImmutableSet.builder();
            if (normalized.size() == 1) {
                if (normalized.iterator().next().annotationType().equals(Named.class)) {
                    builder.add(Default.Literal.INSTANCE);
                }
            }
            builder.add(Any.Literal.INSTANCE);
            builder.addAll(qualifiers);
            normalized = builder.build();
        }
        return normalized;
    }
}
