/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.interceptor.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Marius Bogoevici
 */
public class ReflectionFactoryUtils {
    private static Object reflectionFactory = null;
    private static Method newConstructorForSerialization = null;

    static {
        try {
            Class<?> reflectionFactoryClass = Class.forName("sun.reflect.ReflectionFactory");
            Method getReflectionFactory = reflectionFactoryClass.getDeclaredMethod("getReflectionFactory");
            ReflectionUtils.ensureAccessible(getReflectionFactory);
            reflectionFactory = getReflectionFactory.invoke(null);
            newConstructorForSerialization = reflectionFactoryClass.getDeclaredMethod("newConstructorForSerialization", Class.class, Constructor.class);
            ReflectionUtils.ensureAccessible(newConstructorForSerialization);
        } catch (Exception e) {
            // ignore exceptions, no reflection factory will be available
        }
    }

    public static boolean isAvailable() {
        return reflectionFactory != null && newConstructorForSerialization != null;
    }

    public static <T> Constructor<T> getReflectionFactoryConstructor(Class<T> proxyClass)
            throws NoSuchMethodException {
        if (isAvailable()) {
            try {
                return (Constructor<T>) newConstructorForSerialization.invoke(reflectionFactory, proxyClass, Object.class.getDeclaredConstructor());
            } catch (NoSuchMethodException e) {
                return null;
            } catch (SecurityException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            } catch (InvocationTargetException e) {
                return null;
            }
        }
        return null;
    }
}
