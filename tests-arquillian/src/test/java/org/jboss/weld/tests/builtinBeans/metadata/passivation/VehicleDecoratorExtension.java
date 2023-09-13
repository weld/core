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
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

/**
 * Register {@link VehicleDecorator} as a decorator using {@link PassivationCapableDecoratorImpl}.
 *
 * @author Jozef Hartinger
 *
 */
public class VehicleDecoratorExtension implements Extension {

    void registerVehicleDecorator(@Observes AfterBeanDiscovery event, BeanManager manager) {
        AnnotatedType<VehicleDecorator> annotatedType = manager.createAnnotatedType(VehicleDecorator.class);
        BeanAttributes<VehicleDecorator> attributes = manager.createBeanAttributes(annotatedType);
        Set<Annotation> delegateQualifiers = Collections.<Annotation> singleton(Any.Literal.INSTANCE);
        Set<Type> decoratedTypes = Collections.<Type> singleton(Vehicle.class);
        InjectionTargetFactory<VehicleDecorator> factory = manager.getInjectionTargetFactory(annotatedType);
        event.addBean(new PassivationCapableDecoratorImpl<VehicleDecorator>(VehicleDecorator.class, attributes, Vehicle.class,
                delegateQualifiers, decoratedTypes, factory));
    }
}
