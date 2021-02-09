/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.bean.proxy.util;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.serialization.spi.ProxyServices;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is a default implementation of ProxyServices that will only be loaded if no other implementation is detected.
 * It is only used up until JDK 11 at which point it is replaced by its alternative implementation.
 * <p>
 * This class cracks open the class loader's {@code defineclass} method and then uses it to define new classes.
 */
public class WeldDefaultProxyServices implements ProxyServices {

    private static java.lang.reflect.Method defineClass1, defineClass2;
    private static final AtomicBoolean classLoaderMethodsMadeAccessible = new AtomicBoolean(false);

    /**
     * This method cracks open {@code ClassLoader#defineClass()} methods by calling {@code setAccessible()}.
     * <p>
     * It is invoked during {@code WeldStartup#startContainer()} and only in case the integrator does not
     * fully implement {@link ProxyServices}.
     **/
    public static void makeClassLoaderMethodsAccessible() {
        // the AtomicBoolean make sure this gets invoked only once as WeldStartup is triggered per deployment
        if (classLoaderMethodsMadeAccessible.compareAndSet(false, true)) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        Class<?> cl = Class.forName("java.lang.ClassLoader");
                        final String name = "defineClass";

                        defineClass1 = cl.getDeclaredMethod(name, String.class, byte[].class, int.class, int.class);
                        defineClass2 = cl.getDeclaredMethod(name, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
                        defineClass1.setAccessible(true);
                        defineClass2.setAccessible(true);
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw new RuntimeException("cannot initialize ClassPool", pae.getException());
            }
        }
    }

    public ClassLoader getClassLoader(final Class<?> proxiedBeanType) {
        // TODO remove from API
        throw new IllegalStateException("THIS METHOD IS NO LONGER USED!");
    }


    public Class<?> loadBeanClass(final String className) {
        // TODO remove from API
        throw new IllegalStateException("NO LONGER USED!");
    }

    @Override
    public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len) throws ClassFormatError {
        return defineClass(originalClass, className, classBytes, off, len, null);
    }

    @Override
    public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        try {
            java.lang.reflect.Method method;
            Object[] args;
            if (protectionDomain == null) {
                method = defineClass1;
                args = new Object[]{className, classBytes, 0, len};
            } else {
                method = defineClass2;
                args = new Object[]{className, classBytes, 0, len, protectionDomain};
            }
            ClassLoader loader = originalClass.getClassLoader();
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
                // cannot determine CL, we need to throw an exception
                if (loader == null) {
                    throw BeanLogger.LOG.cannotDetermineClassLoader(className, originalClass);
                }
            }
            Class<?> clazz = (Class) method.invoke(loader, args);
            return clazz;
        } catch (RuntimeException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(Class<?> originalClass, String classBinaryName) throws ClassNotFoundException {
        ClassLoader loader = originalClass.getClassLoader();
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
            // the following should not happen, but if it does, we need to throw an exception
            if (loader == null) {
                throw BeanLogger.LOG.cannotDetermineClassLoader(classBinaryName, originalClass);
            }
        }
        return loader.loadClass(classBinaryName);
    }

    // TODO deprecate in API?
    @Override
    public boolean supportsClassDefining() {
        return true;
    }

    @Override
    public void cleanup() {
        //noop
    }
}
