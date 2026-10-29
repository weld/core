/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.other.OtherPackageBase;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.collections.Multimap;
import org.jboss.weld.util.collections.SetMultimap;
import org.junit.Test;

/**
 * Checks that finding the overridden methods by grouping the methods by their declaring class gives the same
 * result as the original search, which went through all the methods again for every class of the hierarchy.
 */
public class EnhancedAnnotatedTypeOverriddenMethodsTest {

    static class Base<T> {
        public void publicMethod() {
        }

        protected void protectedMethod() {
        }

        void packageMethod() {
        }

        private void privateMethod() {
        }

        public void generic(T value) {
        }
    }

    static class Middle extends Base<String> {
        @Override
        public void publicMethod() {
        }

        @Override
        void packageMethod() {
        }

        private void privateMethod() {
        }

        @Override
        public void generic(String value) {
        }
    }

    static class Leaf extends Middle {
        @Override
        public void publicMethod() {
        }

        @Override
        protected void protectedMethod() {
        }
    }

    // same signature as in OtherPackageBase, but in another package, so it does not override it
    static class CrossPackage extends OtherPackageBase {
        void packageMethod() {
        }

        @Override
        public void publicMethod() {
        }
    }

    private final TypeStore typeStore = new TypeStore();
    private final ClassTransformer transformer = new ClassTransformer(typeStore, new SharedObjectCache(),
            ReflectionCacheFactory.newInstance(typeStore), RegistrySingletonProvider.STATIC_INSTANCE);

    @SuppressWarnings("unchecked")
    private <T> EnhancedAnnotatedTypeImpl<T> enhancedType(Class<T> javaClass) {
        return (EnhancedAnnotatedTypeImpl<T>) transformer.getEnhancedAnnotatedType(javaClass,
                AnnotatedTypeIdentifier.NULL_BDA_ID);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<EnhancedAnnotatedMethod<?, ? super T>> allMethods(EnhancedAnnotatedTypeImpl<T> type) {
        return (Set<EnhancedAnnotatedMethod<?, ? super T>>) (Set<?>) type.getMethods();
    }

    private static Set<String> names(Collection<? extends EnhancedAnnotatedMethod<?, ?>> methods) {
        return methods.stream().map(method -> method.getJavaMember().getDeclaringClass().getSimpleName() + "."
                + method.getJavaMember().getName() + (method.getJavaMember().isBridge() ? "(bridge)" : ""))
                .collect(Collectors.toSet());
    }

    @Test
    public void findsTheMethodsThatAreOverridden() {
        EnhancedAnnotatedTypeImpl<Leaf> leaf = enhancedType(Leaf.class);
        Set<String> effective = names(leaf.getEnhancedMethods());
        Set<String> all = names(allMethods(leaf));

        assertTrue(effective.contains("Leaf.publicMethod"));
        assertFalse(effective.contains("Middle.publicMethod"));
        assertFalse(effective.contains("Base.publicMethod"));
        assertTrue(all.contains("Base.publicMethod"));

        assertTrue(effective.contains("Leaf.protectedMethod"));
        assertFalse(effective.contains("Base.protectedMethod"));

        // private methods are never overridden
        assertTrue(effective.contains("Base.privateMethod"));
        assertTrue(effective.contains("Middle.privateMethod"));

        // a method that is overridden only by a bridge method is kept (WELD-1548)
        assertTrue(effective.contains("Middle.generic"));
        assertTrue(effective.contains("Base.generic"));
        assertTrue(all.contains("Middle.generic(bridge)"));
    }

    @Test
    public void packagePrivateMethodsAreOnlyOverriddenInTheSamePackage() {
        Set<String> effective = names(enhancedType(CrossPackage.class).getEnhancedMethods());
        assertTrue(effective.contains("CrossPackage.packageMethod"));
        assertTrue(effective.contains("OtherPackageBase.packageMethod"));
        assertTrue(effective.contains("CrossPackage.publicMethod"));
        assertFalse(effective.contains("OtherPackageBase.publicMethod"));

        Set<String> inSamePackage = names(enhancedType(Middle.class).getEnhancedMethods());
        assertTrue(inSamePackage.contains("Middle.packageMethod"));
        assertFalse(inSamePackage.contains("Base.packageMethod"));
    }

    @Test
    public void givesTheSameAnswersAsTheOriginalSearch() {
        for (Class<?> javaClass : List.of(Base.class, Middle.class, Leaf.class, CrossPackage.class)) {
            checkSameAnswers(enhancedType(javaClass));
        }
    }

    private <T> void checkSameAnswers(EnhancedAnnotatedTypeImpl<T> type) {
        Set<EnhancedAnnotatedMethod<?, ? super T>> methods = allMethods(type);
        for (boolean skipOverridingBridgeMethods : new boolean[] { false, true }) {
            assertEquals(type.getJavaClass().getSimpleName() + " skipOverridingBridgeMethods=" + skipOverridingBridgeMethods,
                    methodsOf(originalOverriddenMethods(type, methods, skipOverridingBridgeMethods)),
                    methodsOf(type.getOverriddenMethods(type, methods, skipOverridingBridgeMethods)));
        }
    }

    private static Set<Method> methodsOf(Set<? extends EnhancedAnnotatedMethod<?, ?>> methods) {
        return methods.stream().map(EnhancedAnnotatedMethod::getJavaMember).collect(Collectors.toSet());
    }

    // The search as it was before the methods were grouped by their declaring class.

    private <T> Set<EnhancedAnnotatedMethod<?, ? super T>> originalOverriddenMethods(EnhancedAnnotatedTypeImpl<T> type,
            Set<EnhancedAnnotatedMethod<?, ? super T>> methods, boolean skipOverridingBridgeMethods) {
        Set<EnhancedAnnotatedMethod<?, ? super T>> overriddenMethods = new HashSet<>();
        Multimap<MethodSignature, Package> seenMethods = SetMultimap.newSetMultimap();
        for (Class<? super T> clazz = type.getJavaClass(); clazz != null && clazz != Object.class; clazz = clazz
                .getSuperclass()) {
            for (EnhancedAnnotatedMethod<?, ? super T> method : methods) {
                if (method.getJavaMember().getDeclaringClass().equals(clazz)) {
                    if (skipOverridingBridgeMethods && method.getJavaMember().isBridge()) {
                        continue;
                    }
                    if (isOverridden(method, seenMethods)) {
                        overriddenMethods.add(method);
                    }
                    seenMethods.put(method.getSignature(), method.getPackage());
                }
            }
        }
        return overriddenMethods;
    }

    private static boolean isOverridden(EnhancedAnnotatedMethod<?, ?> method,
            Multimap<MethodSignature, Package> seenMethods) {
        if (method.isPrivate()) {
            return false;
        } else if (method.isPackagePrivate() && seenMethods.containsKey(method.getSignature())) {
            return seenMethods.get(method.getSignature()).contains(method.getPackage());
        } else {
            return seenMethods.containsKey(method.getSignature());
        }
    }
}
