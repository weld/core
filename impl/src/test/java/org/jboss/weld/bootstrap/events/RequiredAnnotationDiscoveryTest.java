/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.bootstrap.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.DefaultReflectionCache;
import org.jboss.weld.resources.SharedObjectCache;
import org.junit.Test;

public class RequiredAnnotationDiscoveryTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnClass {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnField {
    }

    @OnClass
    static class Bean {
        @OnField
        String field;
    }

    static class CountingReflectionCache extends DefaultReflectionCache {
        int annotationReads;

        CountingReflectionCache() {
            super(new TypeStore());
        }

        @Override
        public Set<Annotation> getAnnotations(AnnotatedElement element) {
            annotationReads++;
            return super.getAnnotations(element);
        }
    }

    private final CountingReflectionCache cache = new CountingReflectionCache();
    private final RequiredAnnotationDiscovery discovery = new RequiredAnnotationDiscovery(cache);
    private final BackedAnnotatedType<Bean> type = BackedAnnotatedType.of(Bean.class, new SharedObjectCache(), cache,
            "test", AnnotatedTypeIdentifier.NULL_BDA_ID);

    @Test
    public void earlyMatchStopsReflectionWithoutCachingAnIncompleteSet() {
        type.getAnnotations();
        cache.annotationReads = 0;

        assertTrue(discovery.containsAnnotation(type, OnClass.class));
        assertEquals("A direct match must not inspect meta-annotations or members", 0, cache.annotationReads);
        assertTrue(discovery.containsAnnotation(type, OnField.class));
    }

    @Test
    public void completedSearchCachesBothMatchesAndMisses() {
        assertFalse(discovery.containsAnnotation(type, Deprecated.class));
        cache.annotationReads = 0;

        assertTrue(discovery.containsAnnotation(type, OnClass.class));
        assertTrue(discovery.containsAnnotation(type, OnField.class));
        assertFalse(discovery.containsAnnotation(type, FunctionalInterface.class));
        assertEquals("A completed search must avoid reflection for subsequent queries", 0, cache.annotationReads);
    }
}
