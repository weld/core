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
import java.lang.reflect.Modifier;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.logging.ValidatorLogger;
import org.jboss.weld.util.Proxies;

/**
 * Default {@link ProxyInstantiator} implementation that uses proxy class' no-arg constructor to create a new instance.
 *
 * @author Jozef Hartinger
 *
 */
public final class DefaultProxyInstantiator implements ProxyInstantiator {

    public static final ProxyInstantiator INSTANCE = new DefaultProxyInstantiator();

    private DefaultProxyInstantiator() {
    }

    @Override
    public <T> T newInstance(Class<T> clazz)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    @Override
    public <T> UnproxyableResolutionException validateNoargConstructor(Constructor<T> constructor, Class<?> clazz,
            Bean<?> declaringBean)
            throws UnproxyableResolutionException {
        if (constructor == null) {
            return ValidatorLogger.LOG.notProxyableNoConstructor(clazz, Proxies.getDeclaringBeanInfo(declaringBean));
        } else if (Modifier.isPrivate(constructor.getModifiers())) {
            return new UnproxyableResolutionException(
                    ValidatorLogger.LOG.notProxyablePrivateConstructor(clazz.getName(), constructor,
                            Proxies.getDeclaringBeanInfo(declaringBean)));
        }
        return null;
    }

    @Override
    public boolean isUsingConstructor() {
        return true;
    }

    @Override
    public void cleanup() {
    }
}
