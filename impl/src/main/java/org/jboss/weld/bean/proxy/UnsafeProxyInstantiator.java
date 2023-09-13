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
import java.lang.reflect.Field;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.exceptions.UnproxyableResolutionException;

import sun.misc.Unsafe;

/**
 * {@link ProxyInstantiator} implementation that {@link Unsafe#allocateInstance(Class)} to instantiate proxy class instance.
 * When this
 * instantiator is used, the proxy class does not need to declare a no-arg constructor at all.
 *
 * @author Jozef Hartinger
 *
 */
class UnsafeProxyInstantiator implements ProxyInstantiator {

    private final Unsafe unsafe;

    UnsafeProxyInstantiator() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        this.unsafe = (Unsafe) field.get(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newInstance(Class<T> clazz) throws InstantiationException {
        return (T) unsafe.allocateInstance(clazz);
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
