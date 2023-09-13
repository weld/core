/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.bootstrap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.util.reflection.Reflections;

/**
 * This optional extension collects all injection points
 * <ul>
 * <li>of Java EE components,</li>
 * <li>with {@link Instance} required type,</li>
 * </ul>
 * so that Weld is able to identify unused beans better.
 *
 * @author Martin Kouba
 */
public class WeldUnusedMetadataExtension implements Extension {

    private Set<InjectionPoint> componentInjectionPoints;

    private Set<InjectionPoint> instanceInjectionPoints;

    WeldUnusedMetadataExtension() {
        this.componentInjectionPoints = new HashSet<>();
        this.instanceInjectionPoints = new HashSet<>();
    }

    void processInjectionPoints(@Observes ProcessInjectionPoint<?, ?> event) {
        if (event.getInjectionPoint().getBean() == null) {
            componentInjectionPoints.add(event.getInjectionPoint());
        }
        if (Instance.class.equals(Reflections.getRawType(event.getInjectionPoint().getType()))) {
            instanceInjectionPoints.add(event.getInjectionPoint());
        }
    }

    void clear(@Observes @Initialized(ApplicationScoped.class) Object obj) {
        componentInjectionPoints.clear();
        instanceInjectionPoints.clear();
    }

    public boolean isInjectedByEEComponent(Bean<?> bean, BeanManagerImpl beanManager) {
        if (componentInjectionPoints.isEmpty()) {
            return false;
        }
        for (InjectionPoint injectionPoint : componentInjectionPoints) {
            if (beanManager.getBeanResolver().resolve(new ResolvableBuilder(injectionPoint, beanManager).create(), false)
                    .contains(bean)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInstanceResolvedBean(Bean<?> bean, BeanManagerImpl beanManager) {
        if (instanceInjectionPoints.isEmpty()) {
            return false;
        }
        for (InjectionPoint injectionPoint : instanceInjectionPoints) {
            Type facadeType = getFacadeType(injectionPoint);
            if (facadeType != null) {
                Resolvable resolvable = new ResolvableBuilder(facadeType, beanManager)
                        .addQualifiers(injectionPoint.getQualifiers())
                        .setDeclaringBean(injectionPoint.getBean()).create();
                if (beanManager.getBeanResolver().resolve(resolvable, false).contains(bean)) {
                    return true;
                }
            }

        }
        return false;
    }

    private Type getFacadeType(InjectionPoint injectionPoint) {
        Type genericType = injectionPoint.getType();
        if (genericType instanceof ParameterizedType) {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
        return null;
    }

}
