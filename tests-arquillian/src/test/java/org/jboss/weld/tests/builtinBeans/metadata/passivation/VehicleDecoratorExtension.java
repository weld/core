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

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.literal.AnyLiteral;

/**
 * Register {@link VehicleDecorator} as a decorator using {@link PassivationCapableDecoratorImpl}.
 * 
 * @author Jozef Hartinger
 * 
 */
public class VehicleDecoratorExtension implements Extension {

    private PassivationCapableDecoratorImpl<VehicleDecorator> decorator;

    void registerVehicleDecorator(@Observes AfterBeanDiscovery event, BeanManager manager) {
        AnnotatedType<VehicleDecorator> annotatedType = manager.createAnnotatedType(VehicleDecorator.class);
        BeanAttributes<VehicleDecorator> attributes = manager.createBeanAttributes(annotatedType);
        Set<Annotation> delegateQualifiers = Collections.<Annotation> singleton(AnyLiteral.INSTANCE);
        Set<Type> decoratedTypes = Collections.<Type> singleton(Vehicle.class);
        this.decorator = new PassivationCapableDecoratorImpl<VehicleDecorator>(VehicleDecorator.class, attributes,
                Vehicle.class, delegateQualifiers, decoratedTypes);
        InjectionTarget<VehicleDecorator> injectionTarget = manager.createInjectionTarget(annotatedType);
        this.decorator.setInjectionTarget(injectionTarget);
        event.addBean(this.decorator);
    }

    void wrapInjectionPoints(@Observes ProcessInjectionPoint<VehicleDecorator, ?> event) {
        final InjectionPoint delegate = event.getInjectionPoint();
        if (delegate.getBean() == null) {
            event.setInjectionPoint(new ForwardingInjectionPoint() {

                @Override
                public Bean<?> getBean() {
                    return decorator;
                }

                @Override
                protected InjectionPoint delegate() {
                    return delegate;
                }
            });
        }
    }
}
