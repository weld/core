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
package org.jboss.weld.util.bean;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;

/**
 * Used within {@link IsolatedForwardingBean} and its subclasses as a value holder.
 *
 * @author Jozef Hartinger
 *
 * @param <T> type of the bean class
 * @param <S> type of bean (either Bean, Interceptor or Decorator)
 */
public class WrappedBeanHolder<T, S extends Bean<T>> {

    public static <T, S extends Bean<T>> WrappedBeanHolder<T, S> of(BeanAttributes<T> attributes, S bean) {
        return new WrappedBeanHolder<T, S>(attributes, bean);
    }

    private final BeanAttributes<T> attributes;
    private final S bean;

    public WrappedBeanHolder(BeanAttributes<T> attributes, S bean) {
        this.attributes = attributes;
        this.bean = bean;
    }

    public BeanAttributes<T> getAttributes() {
        return attributes;
    }

    public S getBean() {
        return bean;
    }
}
