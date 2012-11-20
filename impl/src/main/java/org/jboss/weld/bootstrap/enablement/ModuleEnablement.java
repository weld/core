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

import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATOR_SPECIFIED_TWICE;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTOR_SPECIFIED_TWICE;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.ValidatorMessage;

/**
 * Holds information about interceptors, decorators and alternatives that enabled in this module.
 *
 * @author Jozef Hartinger
 *
 */
public class ModuleEnablement {

    public static final ModuleEnablement EMPTY_ENABLEMENT = new ModuleEnablement(Collections.<ClassEnablement>emptyList(), Collections.<ClassEnablement>emptyList(), Collections.<ClassEnablement>emptyList());

    private final List<ClassEnablement> interceptors;
    private final List<ClassEnablement> decorators;
    private final List<ClassEnablement> alternatives;

    // fast lookup structures
    private final Map<String, Integer> interceptorMap;
    private final Map<String, Integer> decoratorMap;

    private final Map<String, ClassEnablement> alternativeMap;

    private final Comparator<Decorator<?>> decoratorComparator;
    private final Comparator<Interceptor<?>> interceptorComparator;

    public ModuleEnablement(List<ClassEnablement> interceptors, List<ClassEnablement> decorators,
            List<ClassEnablement> alternatives) {
        this.interceptors = interceptors;
        this.decorators = decorators;
        this.alternatives = alternatives;

        this.interceptorMap = createLookupMap(interceptors, INTERCEPTOR_SPECIFIED_TWICE);
        this.decoratorMap = createLookupMap(decorators, DECORATOR_SPECIFIED_TWICE);

        if (alternatives.isEmpty()) {
            this.alternativeMap = Collections.emptyMap();
        } else {
            Map<String, ClassEnablement> alternativeMap = new HashMap<String, ClassEnablement>();
            for (ClassEnablement alternative : alternatives) {
                alternativeMap.put(alternative.getEnabledClass().getName(), alternative);
            }
            this.alternativeMap = Collections.unmodifiableMap(alternativeMap);
        }

        this.decoratorComparator = new EnablementComparator<Decorator<?>>(decoratorMap);
        this.interceptorComparator = new EnablementComparator<Interceptor<?>>(interceptorMap);
    }

    private static Map<String, Integer> createLookupMap(List<ClassEnablement> list, ValidatorMessage specifiedTwiceMessage) {
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (int i = 0; i < list.size(); i++) {
            Integer previousOccurence = result.put(list.get(i).getEnabledClass().getName(), i);
            if (previousOccurence != null) {
                throw new DeploymentException(specifiedTwiceMessage, list.get(i).getEnabledClass().getName(), list.get(i), list.get(previousOccurence));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public boolean isAlternativeEnabled(Class<?> javaClass) {
        return alternativeMap.containsKey(javaClass.getName());
    }

    public ClassEnablement getAlternative(Class<?> javaClass) {
        return alternativeMap.get(javaClass.getName());
    }

    public boolean isInterceptorEnabled(Class<?> javaClass) {
        return interceptorMap.containsKey(javaClass.getName());
    }

    public boolean isDecoratorEnabled(Class<?> javaClass) {
        return decoratorMap.containsKey(javaClass.getName());
    }

    public List<ClassEnablement> getInterceptors() {
        return interceptors;
    }

    public List<ClassEnablement> getDecorators() {
        return decorators;
    }

    public List<ClassEnablement> getAlternatives() {
        return alternatives;
    }

    public Comparator<Decorator<?>> getDecoratorComparator() {
        return decoratorComparator;
    }

    public Comparator<Interceptor<?>> getInterceptorComparator() {
        return interceptorComparator;
    }

    private static class EnablementComparator<T extends Bean<?>> implements Comparator<T> {

        private final Map<String, Integer> enabledClasses;

        public EnablementComparator(Map<String, Integer> enabledClasses) {
            this.enabledClasses = enabledClasses;
        }

        @Override
        public int compare(T o1, T o2) {
            int p1 = enabledClasses.get(o1.getBeanClass().getName());
            int p2 = enabledClasses.get(o2.getBeanClass().getName());
            return p1 - p2;
        }
    }
}
