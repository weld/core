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
package org.jboss.weld.tests.disposer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

/**
 * An extension that registers Sportscar factory.
 * 
 * @author Jozef Hartinger
 * 
 */
public class SportscarFactoryExtension implements Extension {

    private Producer<Car> producer;

    public void observeCarFactory(@Observes ProcessProducer<CarFactory, Car> event) {
        producer = event.getProducer();
    }

    public void registerSportsCarFactory(@Observes AfterBeanDiscovery event) {
        event.addBean(new Bean<Car>() {

            public Car create(CreationalContext<Car> creationalContext) {
                return producer.produce(creationalContext);
            }

            public void destroy(Car instance, CreationalContext<Car> creationalContext) {
                try {
                    producer.dispose(instance);
                } finally {
                    creationalContext.release();
                }
            }

            public Set<Type> getTypes() {
                return Collections.<Type>singleton(Car.class);
            }

            public Set<Annotation> getQualifiers() {
                return Collections.<Annotation>singleton(new Sport.Literal());
            }

            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            public String getName() {
                return null;
            }

            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            public boolean isAlternative() {
                return false;
            }

            public boolean isNullable() {
                return false;
            }

            public Class<?> getBeanClass() {
                return CarFactory.class;
            }

            public Set<InjectionPoint> getInjectionPoints() {
                return producer.getInjectionPoints();
            }
        });
    }
}
