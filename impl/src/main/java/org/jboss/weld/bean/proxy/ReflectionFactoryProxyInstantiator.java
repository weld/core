/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.WeldException;

import sun.reflect.ReflectionFactory;

/**
 * {@link ProxyInstantiator} implementation that uses
 * {@link ReflectionFactory#newConstructorForSerialization(Class, Constructor)} for creating instances of a
 * proxy class.
 *
 * @author Jozef Hartinger
 *
 */
class ReflectionFactoryProxyInstantiator implements ProxyInstantiator {

    private final ReflectionFactory factory;
    private final Constructor<Object> constructor;

    ReflectionFactoryProxyInstantiator() {
        this.factory = ReflectionFactory.getReflectionFactory();
        try {
            this.constructor = Object.class.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new WeldException(e); // this should never happen!
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        try {
            return (T) factory.newConstructorForSerialization(clazz, constructor).newInstance();
        } catch (IllegalArgumentException e) {
            throw new WeldException(e);
        } catch (InvocationTargetException e) {
            throw new WeldException(e);
        }
    }

    @Override
    public boolean isUsingConstructor() {
        return false;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public <T> UnproxyableResolutionException validateNoargConstructor(Constructor<T> constructor, Class<?> clazz,
            Bean<?> declaringBean)
            throws UnproxyableResolutionException {
        // noop - no constructor is fine
        return null;
    }
}
