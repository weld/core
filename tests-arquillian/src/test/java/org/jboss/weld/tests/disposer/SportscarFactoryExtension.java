/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
