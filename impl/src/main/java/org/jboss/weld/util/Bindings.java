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

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.resolution.QualifierInstance;

import com.google.common.base.Preconditions;

/**
 * Utility methods for working with qualifiers and interceptor bindings.
 *
 * @author Jozef Hartinger
 *
 */
public class Bindings {

    private Bindings() {
    }

    public static boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2, MetaAnnotationStore store) {
        checkQualifier(qualifier1, store);
        checkQualifier(qualifier2, store);

        QualifierInstance q1 = store.getQualifierInstance(qualifier1);
        QualifierInstance q2 = store.getQualifierInstance(qualifier2);
        return q1.equals(q2);
    }

    public static int getQualifierHashCode(Annotation qualifier, MetaAnnotationStore store) {
        checkQualifier(qualifier, store);
        return store.getQualifierInstance(qualifier).hashCode();
    }

    private static void checkQualifier(Annotation qualifier, MetaAnnotationStore store) {
        Preconditions.checkNotNull(qualifier);

        QualifierModel<?> model = store.getBindingTypeModel(qualifier.annotationType());
        if (model == null || !model.isValid()) {
            throw BeanManagerLogger.LOG.invalidQualifier(qualifier);
        }
    }

    public static void validateQualifiers(Iterable<Annotation> qualifiers, BeanManager manager, Object definer) {
        for (Annotation annotation : qualifiers) {
            if (!manager.isQualifier(annotation.annotationType())) {
                throw MetadataLogger.LOG.notAQualifier(annotation.annotationType(), definer);
            }
        }
    }

    public static boolean areInterceptorBindingsEquivalent(Annotation qualifier1, Annotation qualifier2, MetaAnnotationStore store) {
        checkInterceptorBinding(qualifier1, store);
        checkInterceptorBinding(qualifier2, store);

        QualifierInstance q1 = store.getQualifierInstance(qualifier1);
        QualifierInstance q2 = store.getQualifierInstance(qualifier2);
        return q1.equals(q2);
    }

    public static int getInterceptorBindingHashCode(Annotation qualifier, MetaAnnotationStore store) {
        checkInterceptorBinding(qualifier, store);
        return store.getQualifierInstance(qualifier).hashCode();
    }

    private static void checkInterceptorBinding(Annotation qualifier, MetaAnnotationStore store) {
        Preconditions.checkNotNull(qualifier);

        InterceptorBindingModel<?> model = store.getInterceptorBindingModel(qualifier.annotationType());
        if (model == null || !model.isValid()) {
            throw BeanManagerLogger.LOG.interceptorResolutionWithNonbindingType(qualifier);
        }
    }
}
