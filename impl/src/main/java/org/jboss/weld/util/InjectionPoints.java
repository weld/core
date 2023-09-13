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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bootstrap.MissingDependenciesRegistry;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPointImpl;
import org.jboss.weld.injection.attributes.ForwardingFieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.SpecialParameterInjectionPoint;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.logging.ValidatorLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Helper class for {@link InjectionPoint} processing.
 *
 * @author Jozef Hartinger
 *
 */
public class InjectionPoints {

    private InjectionPoints() {
    }

    public static <T extends WeldInjectionPointAttributes<?, ?>> Set<T> flattenInjectionPoints(
            List<? extends Set<T>> fieldInjectionPoints) {
        Set<T> injectionPoints = new HashSet<T>();
        for (Set<T> i : fieldInjectionPoints) {
            injectionPoints.addAll(i);
        }
        return injectionPoints;
    }

    public static Set<ParameterInjectionPoint<?, ?>> flattenParameterInjectionPoints(
            List<Set<MethodInjectionPoint<?, ?>>> methodInjectionPoints) {
        Set<ParameterInjectionPoint<?, ?>> injectionPoints = new HashSet<ParameterInjectionPoint<?, ?>>();
        for (Set<MethodInjectionPoint<?, ?>> i : methodInjectionPoints) {
            for (MethodInjectionPoint<?, ?> method : i) {
                for (ParameterInjectionPoint<?, ?> parameter : method.getParameterInjectionPoints()) {
                    injectionPoints.add(parameter);
                }
            }
        }
        return injectionPoints;
    }

    public static <X> Set<InjectionPoint> filterOutSpecialParameterInjectionPoints(
            List<ParameterInjectionPoint<?, X>> injectionPoints) {
        ImmutableSet.Builder<InjectionPoint> filtered = ImmutableSet.builder();
        for (ParameterInjectionPoint<?, X> parameter : injectionPoints) {
            if (parameter instanceof SpecialParameterInjectionPoint) {
                continue;
            }
            filtered.add(parameter);
        }
        return filtered.build();
    }

    public static <T, X> WeldInjectionPointAttributes<T, ?> getWeldInjectionPoint(InjectionPoint injectionPoint) {
        if (injectionPoint instanceof WeldInjectionPointAttributes<?, ?>) {
            return Reflections.cast(injectionPoint);
        }
        if (injectionPoint.getAnnotated() instanceof AnnotatedField<?>) {
            return FieldInjectionPoint.<T, X> silent(ForwardingFieldInjectionPointAttributes.<T, X> of(injectionPoint));
        } else {
            return ParameterInjectionPointImpl
                    .<T, X> silent(ForwardingParameterInjectionPointAttributes.<T, X> of(injectionPoint));
        }
    }

    /**
     *
     * @param bean
     * @param resolvedBean
     * @return <code>true</code> if the container is permitted to optimize an injectable reference lookup, <code>false</code>
     *         otherwise
     * @see http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#injectable_reference
     */
    public static boolean isInjectableReferenceLookupOptimizationAllowed(Bean<?> bean, Bean<?> resolvedBean) {
        Preconditions.checkArgumentNotNull(resolvedBean, "resolvedBean");
        return bean != null
                && ((RequestScoped.class.equals(bean.getScope()) && Beans.hasBuiltinScope(resolvedBean))
                        || (ApplicationScoped.class.equals(bean.getScope()) && ApplicationScoped.class
                                .equals(resolvedBean.getScope())));
    }

    public static String getUnsatisfiedDependenciesAdditionalInfo(InjectionPoint ij, BeanManagerImpl beanManager) {
        Set<Bean<?>> beansMatchedByType = beanManager.getBeans(ij.getType(), Any.Literal.INSTANCE);
        if (beansMatchedByType.isEmpty()) {
            Class<?> rawType = Reflections.getRawType(ij.getType());
            if (rawType != null) {
                MissingDependenciesRegistry missingDependenciesRegistry = beanManager.getServices()
                        .get(MissingDependenciesRegistry.class);
                String missingDependency = missingDependenciesRegistry.getMissingDependencyForClass(rawType.getName());
                if (missingDependency != null) {
                    return ValidatorLogger.LOG.unsatisfiedDependencyBecauseClassIgnored(
                            rawType.getName(),
                            missingDependency);
                }
            }
        } else {
            return ValidatorLogger.LOG.unsatisfiedDependencyBecauseQualifiersDontMatch(
                    WeldCollections.toMultiRowString(beansMatchedByType));
        }
        return "";
    }

}
