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
package org.jboss.weld.bootstrap.enablement;

import static org.jboss.weld.util.collections.Sets.union;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.util.collections.ImmutableMap;

/**
 * Holds information about interceptors, decorators and alternatives that are enabled in this module.
 *
 * @author Jozef Hartinger
 *
 */
public class ModuleEnablement {

    public static final ModuleEnablement EMPTY_ENABLEMENT = new ModuleEnablement(Collections.<Class<?>> emptyList(),
            Collections.<Class<?>> emptyList(), Collections.<Class<?>, Integer> emptyMap(), Collections.<Class<?>> emptySet(),
            Collections.<Class<? extends Annotation>> emptySet());

    private final List<Class<?>> interceptors;
    private final List<Class<?>> decorators;

    private final Map<Class<?>, Integer> interceptorMap;
    private final Map<Class<?>, Integer> decoratorMap;
    private final Map<Class<?>, Integer> globalAlternatives;

    private final Set<Class<?>> localAlternativeClasses;
    private final Set<Class<? extends Annotation>> localAlternativeStereotypes;

    private final Comparator<Decorator<?>> decoratorComparator;
    private final Comparator<Interceptor<?>> interceptorComparator;

    public ModuleEnablement(List<Class<?>> interceptors, List<Class<?>> decorators, Map<Class<?>, Integer> globalAlternatives,
            Set<Class<?>> localAlternativeClasses, Set<Class<? extends Annotation>> localAlternativeStereotypes) {
        this.interceptors = interceptors;
        this.decorators = decorators;

        this.interceptorMap = createLookupMap(interceptors);
        this.decoratorMap = createLookupMap(decorators);

        this.decoratorComparator = new EnablementComparator<Decorator<?>>(decoratorMap);
        this.interceptorComparator = new EnablementComparator<Interceptor<?>>(interceptorMap);

        this.globalAlternatives = globalAlternatives;

        this.localAlternativeClasses = localAlternativeClasses;
        this.localAlternativeStereotypes = localAlternativeStereotypes;
    }

    private static Map<Class<?>, Integer> createLookupMap(List<Class<?>> list) {
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Class<?>, Integer> result = new HashMap<Class<?>, Integer>();
        for (int i = 0; i < list.size(); i++) {
            result.put(list.get(i), i);
        }
        return ImmutableMap.copyOf(result);
    }

    public boolean isInterceptorEnabled(Class<?> javaClass) {
        return interceptorMap.containsKey(javaClass);
    }

    public boolean isDecoratorEnabled(Class<?> javaClass) {
        return decoratorMap.containsKey(javaClass);
    }

    public List<Class<?>> getInterceptors() {
        return interceptors;
    }

    public List<Class<?>> getDecorators() {
        return decorators;
    }

    public Comparator<Decorator<?>> getDecoratorComparator() {
        return decoratorComparator;
    }

    public Comparator<Interceptor<?>> getInterceptorComparator() {
        return interceptorComparator;
    }

    public Integer getAlternativePriority(Class<?> javaClass) {
        return globalAlternatives.get(javaClass);
    }

    public boolean isEnabledAlternativeClass(Class<?> alternativeClass) {
        return globalAlternatives.containsKey(alternativeClass) || localAlternativeClasses.contains(alternativeClass);
    }

    public boolean isEnabledAlternativeStereotype(Class<?> alternativeClass) {
        return globalAlternatives.containsKey(alternativeClass) || localAlternativeStereotypes.contains(alternativeClass);
    }

    public Set<Class<?>> getAlternativeClasses() {
        return localAlternativeClasses;
    }

    public Set<Class<? extends Annotation>> getAlternativeStereotypes() {
        return localAlternativeStereotypes;
    }

    public Set<Class<?>> getGlobalAlternatives() {
        return globalAlternatives.keySet();
    }

    public Set<Class<?>> getAllAlternatives() {
        return union(union(localAlternativeClasses, localAlternativeStereotypes), getGlobalAlternatives());
    }

    private static class EnablementComparator<T extends Bean<?>> implements Comparator<T>, Serializable {

        private static final long serialVersionUID = -4757462262711016985L;
        private final Map<Class<?>, Integer> enabledClasses;

        public EnablementComparator(Map<Class<?>, Integer> enabledClasses) {
            this.enabledClasses = enabledClasses;
        }

        @Override
        public int compare(T o1, T o2) {
            int p1 = enabledClasses.get(o1.getBeanClass());
            int p2 = enabledClasses.get(o2.getBeanClass());
            return p1 - p2;
        }
    }

    @Override
    public String toString() {
        return "ModuleEnablement [interceptors=" + interceptors + ", decorators=" + decorators + ", alternatives="
                + getAllAlternatives() + "]";
    }
}
