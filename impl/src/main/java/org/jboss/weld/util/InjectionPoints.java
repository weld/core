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

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPointImpl;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.injection.attributes.ForwardingFieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.SpecialParameterInjectionPoint;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Helper class for {@link InjectionPoint} processing.
 * @author Jozef Hartinger
 *
 */
public class InjectionPoints {

    private InjectionPoints() {
    }

    public static <T extends WeldInjectionPoint<?, ?>> Set<T> flattenInjectionPoints(List<? extends Set<T>> fieldInjectionPoints) {
        ArraySet<T> injectionPoints = new ArraySet<T>();
        for (Set<T> i : fieldInjectionPoints) {
            injectionPoints.addAll(i);
        }
        return injectionPoints.trimToSize();
    }

    public static Set<ParameterInjectionPoint<?, ?>> flattenParameterInjectionPoints(List<Set<MethodInjectionPoint<?, ?>>> methodInjectionPoints) {
        ArraySet<ParameterInjectionPoint<?, ?>> injectionPoints = new ArraySet<ParameterInjectionPoint<?, ?>>();
        for (Set<MethodInjectionPoint<?, ?>> i : methodInjectionPoints) {
            for (MethodInjectionPoint<?, ?> method : i) {
                for (ParameterInjectionPoint<?, ?> parameter : method.getParameterInjectionPoints()) {
                    injectionPoints.add(parameter);
                }
            }
        }
        return injectionPoints.trimToSize();
    }

    public static <X> Set<ParameterInjectionPoint<?, X>> filterOutSpecialParameterInjectionPoints(List<ParameterInjectionPoint<?, X>> injectionPoints) {
        ArraySet<ParameterInjectionPoint<?, X>> filtered = new ArraySet<ParameterInjectionPoint<?, X>>();
        for (ParameterInjectionPoint<?, X> parameter : injectionPoints) {
            if (parameter instanceof SpecialParameterInjectionPoint) {
                continue;
            }
            filtered.add(parameter);
        }
        return filtered.trimToSize();
    }

    public static InjectionPoint getDelegateInjectionPoint(javax.enterprise.inject.spi.Decorator<?> decorator) {
        if (decorator instanceof DecoratorImpl<?>) {
            return ((DecoratorImpl<?>) decorator).getDelegateInjectionPoint();
        } else {
            for (InjectionPoint injectionPoint : decorator.getInjectionPoints()) {
                if (injectionPoint.isDelegate())
                    return injectionPoint;
            }
        }
        return null;
    }

    public static <T, X> WeldInjectionPoint<T, ?> getWeldInjectionPoint(InjectionPoint injectionPoint) {
        if (injectionPoint instanceof WeldInjectionPoint<?, ?>) {
            return Reflections.cast(injectionPoint);
        }
        if (injectionPoint.getAnnotated() instanceof AnnotatedField<?>) {
            return FieldInjectionPoint.<T, X>silent(ForwardingFieldInjectionPointAttributes.<T, X>of(injectionPoint));
        } else {
            return ParameterInjectionPointImpl.<T, X>silent(ForwardingParameterInjectionPointAttributes.<T, X>of(injectionPoint));
        }
    }
}
