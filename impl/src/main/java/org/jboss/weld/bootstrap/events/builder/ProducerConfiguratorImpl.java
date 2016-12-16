/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events.builder;

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.inject.spi.configurator.ProducerConfigurator;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class ProducerConfiguratorImpl<T> implements ProducerConfigurator<T> {

    private Function<CreationalContext<T>, T> produceCallback;

    private Consumer<T> disposeCallback;

    private final Set<InjectionPoint> injectionPoints;

    /**
     *
     * @param producer
     */
    public ProducerConfiguratorImpl(Producer<T> producer) {
        this.produceCallback = (c) -> producer.produce(c);
        this.disposeCallback = (i) -> producer.dispose(i);
        this.injectionPoints = new HashSet<>(producer.getInjectionPoints());
    }

    @Override
    public <U extends T> ProducerConfigurator<T> produceWith(Function<CreationalContext<U>, U> callback) {
        checkArgumentNotNull(callback);
        this.produceCallback = cast(callback);
        return this;
    }

    @Override
    public ProducerConfigurator<T> disposeWith(Consumer<T> callback) {
        checkArgumentNotNull(callback);
        this.disposeCallback = cast(callback);
        return this;
    }

    @Override
    public ProducerConfigurator<T> addInjectionPoint(InjectionPoint injectionPoint) {
        checkArgumentNotNull(injectionPoint);
        this.injectionPoints.add(injectionPoint);
        return this;
    }

    @Override
    public ProducerConfigurator<T> addInjectionPoints(InjectionPoint... injectionPoints) {
        Collections.addAll(this.injectionPoints, injectionPoints);
        return this;
    }

    @Override
    public ProducerConfigurator<T> addInjectionPoints(Set<InjectionPoint> injectionPoints) {
        checkArgumentNotNull(injectionPoints);
        this.injectionPoints.addAll(injectionPoints);
        return this;
    }

    @Override
    public ProducerConfigurator<T> injectionPoints(InjectionPoint... injectionPoints) {
        this.injectionPoints.clear();
        return addInjectionPoints(injectionPoints);
    }

    Function<CreationalContext<T>, T> getProduceCallback() {
        return produceCallback;
    }

    Consumer<T> getDisposeCallback() {
        return disposeCallback;
    }

    Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

}
