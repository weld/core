/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.interceptor.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class ReflectionUtils {

    private static final Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>();

    static {
        Class[] primitiveTypes = {byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class, void.class};
        for (Class<?> primitiveType : primitiveTypes) {
            PRIMITIVES.put(primitiveType.getName(), primitiveType);
        }
    }

    public static void ensureAccessible(final Method method) {
        doSecurely(new PrivilegedAction<Object>() {
            public Object run() {
                method.setAccessible(true);
                return null;
            }
        });

    }

    public static Class<?> classForName(String name) throws ClassNotFoundException {
        try {
            return classForName0(name);
        } catch (ClassNotFoundException e) {
            Class<?> primitive = PRIMITIVES.get(name);
            if (primitive != null) {
                return primitive;
            } else {
                throw e;
            }
        }
    }

    private static Class<?> classForName0(String className) throws ClassNotFoundException {
        ClassLoader threadContextClassLoader = getThreadContextClassLoader();
        if (threadContextClassLoader != null) {
            return Class.forName(className, true, threadContextClassLoader);
        } else {
            return Class.forName(className);
        }
    }

    public static ClassLoader getThreadContextClassLoader() {
        return doSecurely(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private static <O> O doSecurely(final PrivilegedAction<O> action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            return AccessController.doPrivileged(action);
        } else {
            return action.run();
        }
    }

}
