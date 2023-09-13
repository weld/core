/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Implementations of this interface are capable of performing field/method injection as defined in
 * {@link InjectionTarget#inject(Object, CreationalContext)}.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public interface Injector<T> {

    void inject(T instance, CreationalContext<T> ctx, BeanManagerImpl manager, SlimAnnotatedType<T> type,
            InjectionTarget<T> injectionTarget);

    /**
     * Add field/parameter injection points to the set of injection points of an InjectionTarget. The resulting set is returned
     * from {@link InjectionTarget#getInjectionPoints()}.
     *
     * @param injectionPoints
     */
    void registerInjectionPoints(Set<InjectionPoint> injectionPoints);

    List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods();

    List<Set<FieldInjectionPoint<?, ?>>> getInjectableFields();
}
