/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.annotated.enhanced.jlr;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.junit.Test;

public class EnhancedAnnotatedTypeOverriddenMethodsTest {

    static class Base {
        public Object value() {
            return null;
        }
    }

    static class Middle extends Base {
        @Override
        public String value() {
            return "";
        }
    }

    static class Leaf extends Middle {
        @Override
        public String value() {
            return "";
        }
    }

    private final TypeStore typeStore = new TypeStore();
    private final ClassTransformer transformer = new ClassTransformer(typeStore, new SharedObjectCache(),
            ReflectionCacheFactory.newInstance(typeStore), RegistrySingletonProvider.STATIC_INSTANCE);

    private <T> EnhancedAnnotatedType<T> enhancedType(Class<T> javaClass) {
        return transformer.getEnhancedAnnotatedType(javaClass,
                AnnotatedTypeIdentifier.NULL_BDA_ID);
    }

    private static Set<Method> nonBridgeMethods(Collection<? extends AnnotatedMethod<?>> methods) {
        return methods.stream().map(AnnotatedMethod::getJavaMember).filter(method -> !method.isBridge())
                .collect(Collectors.toSet());
    }

    @Test
    public void resolvesCovariantOverridesAcrossTheHierarchy() throws NoSuchMethodException {
        EnhancedAnnotatedType<Leaf> leaf = enhancedType(Leaf.class);
        assertEquals(Set.of(Leaf.class.getDeclaredMethod("value")), nonBridgeMethods(leaf.getEnhancedMethods()));
        assertEquals(Set.of(Base.class.getDeclaredMethod("value"), Middle.class.getDeclaredMethod("value"),
                Leaf.class.getDeclaredMethod("value")), nonBridgeMethods(leaf.getMethods()));
    }
}
