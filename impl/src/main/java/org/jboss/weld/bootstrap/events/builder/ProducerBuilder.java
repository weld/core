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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 * @author Martin Kouba
 */
public class ProducerBuilder<T> {

    private final ProducerConfiguratorImpl<T> configurator;

    /**
     *
     * @param configurator
     */
    public ProducerBuilder(ProducerConfiguratorImpl<T> configurator) {
        this.configurator = configurator;
    }

    public ProducerConfiguratorImpl<T> configure() {
        return configurator;
    }

    public Producer<T> build() {
        return new ImmutableProducer<>(configurator);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    static class ImmutableProducer<T> implements Producer<T> {

        private final Function<CreationalContext<T>, T> produceCallback;

        private final Consumer<T> disposeCallback;

        private final Set<InjectionPoint> injectionPoints;

        ImmutableProducer(ProducerConfiguratorImpl<T> configurator) {
            this.injectionPoints = configurator.getInjectionPoints().stream().filter((e) -> e != null).collect(ImmutableSet.collector());
            this.produceCallback = configurator.getProduceCallback();
            this.disposeCallback = configurator.getDisposeCallback();
        }

        @Override
        public T produce(CreationalContext<T> ctx) {
            return produceCallback.apply(ctx);
        }

        @Override
        public void dispose(T instance) {
            disposeCallback.accept(instance);
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }

    }

}
