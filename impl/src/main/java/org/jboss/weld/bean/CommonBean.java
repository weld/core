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
package org.jboss.weld.bean;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Common superclass for beans that are identified using id.
 *
 * @author Jozef Hartinger
 * @author Pete Muir
 *
 */
public abstract class CommonBean<T> extends ForwardingBeanAttributes<T> implements Bean<T>, WeldBean<T> {

    private volatile BeanAttributes<T> attributes;

    private final BeanIdentifier identifier;

    protected CommonBean(BeanAttributes<T> attributes, BeanIdentifier identifier) {
        this.attributes = attributes;
        this.identifier = identifier;
    }

    protected Object unwrap(Object object) {
        if (object instanceof ForwardingBean<?>) {
            return Reflections.<ForwardingBean<?>> cast(object).delegate();
        }
        if (object instanceof ForwardingInterceptor<?>) {
            return Reflections.<ForwardingInterceptor<?>> cast(object).delegate();
        }
        if (object instanceof ForwardingDecorator<?>) {
            return Reflections.<ForwardingDecorator<?>> cast(object).delegate();
        }
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        Object object = unwrap(obj);
        if (this == obj) {
            return true;
        }
        if (object instanceof CommonBean<?>) {
            CommonBean<?> that = (CommonBean<?>) object;
            return this.getIdentifier().equals(that.getIdentifier());
        }
        return false;
    }

    protected BeanAttributes<T> attributes() {
        return attributes;
    }

    public void setAttributes(BeanAttributes<T> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    public String getId() {
        return identifier.asString();
    }

    public BeanIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return getId();
    }
}
