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

import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.serialization.spi.ProxyServices;

import java.lang.invoke.MethodHandles;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is a default implementation of ProxyServices that will only be loaded if no other implementation is detected.
 * It supports class defining and attempts to use {@link MethodHandles.Lookup} if possible making it JDK  11+ friendly.
 * For classes in signed JARs and classes from Java internal packages, we are forced to use custom class loader.
 */
public class WeldDefaultProxyServices implements ProxyServices {

    // a map of parent CL -> our CL serving as a cache
    private ConcurrentMap<ClassLoader, WeldProxyDeclaringCL> clMap = new ConcurrentHashMap<ClassLoader, WeldProxyDeclaringCL>();

    @Override
    public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len) throws ClassFormatError {
        return defineClass(originalClass, className, classBytes, off, len, null);
    }

    @Override
    public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        ClassLoader originalLoader = originalClass.getClassLoader();
        if (originalLoader == null) {
            originalLoader = Thread.currentThread().getContextClassLoader();
            // if it's still null we cannot solve this issue, and we need to throw an exception
            if (originalLoader == null) {
                throw BeanLogger.LOG.cannotDetermineClassLoader(className, originalClass);
            }
        }
        try {
            // this is one of the classes we define into our own packages, we need to use a CL approach
            if (className.startsWith(ProxyFactory.WELD_PROXY_PREFIX)) {
                return defineWithClassLoader(className, classBytes, classBytes.length, originalLoader, protectionDomain);
            } else {
                // these classes go into existing packages, we will use MethodHandles to define them
                return defineWithMethodLookup(className, classBytes, originalClass, originalLoader);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(Class<?> originalClass, String classBinaryName) throws ClassNotFoundException {
        ClassLoader loader = originalClass.getClassLoader();
        // if the CL is null, we will use TCCL
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
            // the following should not happen, but if it does, we need to throw an exception
            if (loader == null) {
                throw BeanLogger.LOG.cannotDetermineClassLoader(classBinaryName, originalClass);
            }
        }
        if (clMap.containsKey(loader)) {
            loader = clMap.get(loader);
        }
        return loader.loadClass(classBinaryName);
    }

    @Override
    public void cleanup() {
        clMap.clear();
    }

    /**
     *  Defines the proxy with {@link WeldProxyDeclaringCL}.
     *
     * @param classToDefineName name of the class to be defined
     * @param classBytes class bytes
     * @param length length of the class data
     * @param loader class loader to be wrapped
     * @param domain {@link ProtectionDomain}
     * @return
     */
    private Class<?> defineWithClassLoader(String classToDefineName, byte[] classBytes, int length, ClassLoader loader, ProtectionDomain domain) {
        WeldProxyDeclaringCL delegatingClassLoader = returnWeldCL(loader);
        if (domain == null) {
            return delegatingClassLoader.publicDefineClass(classToDefineName, classBytes, 0, length);
        } else {
            return delegatingClassLoader.publicDefineClass(classToDefineName, classBytes, 0, length, domain);
        }
    }

    /**
     * For a given {@link ClassLoader}, return relevant {@link WeldProxyDeclaringCL}.
     *
     * @param loader class loader
     * @return instance of {@link WeldProxyDeclaringCL}
     */
    private WeldProxyDeclaringCL returnWeldCL(ClassLoader loader) {
        if (loader instanceof WeldProxyDeclaringCL) {
            return (WeldProxyDeclaringCL) loader;
        }
        return clMap.computeIfAbsent(loader, cl -> new WeldProxyDeclaringCL(cl));
    }

    /**
     * Defines the proxy using {@link MethodHandles.Lookup}.
     *
     * @param classToDefineName name of the class to be defined
     * @param classBytes class bytes
     * @param originalClass the original class from which we derived this proxy; this is used to get {@Lookup} object
     * @param loader class loader that loaded the original class
     * @return
     */
    private Class<?> defineWithMethodLookup(String classToDefineName, byte[] classBytes, Class<?> originalClass, ClassLoader loader) {
        Module thisModule = WeldDefaultProxyServices.class.getModule();
        try {
            Class<?> lookupBaseClass;
            try {
                // In case of decorators, it looks like we sometimes need the original class instead
                lookupBaseClass = loader.loadClass(classToDefineName.substring(0, classToDefineName.indexOf("$")));
            } catch (Exception e) {
                lookupBaseClass = originalClass;
            }
            Module lookupClassModule = lookupBaseClass.getModule();
            if (!thisModule.canRead(lookupClassModule)) {
                // we need to read the other module in order to have privateLookup access
                // see javadoc for MethodHandles.privateLookupIn()
                thisModule.addReads(lookupClassModule);
            }
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(lookupBaseClass, MethodHandles.lookup());
            return lookup.defineClass(classBytes);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A class loader that should only be used to load Weld-prefixed proxies meaning those that have non-existent packages.
     * This is a workaround for JMPS approach ({@code MethodHandles.Lookup}) not being able to fulfil this scenario.
     *
     * This CL will cause issues if used to define proxies for beans with private access (which shouldn't happen).
     * It also makes (de)serialization) difficult because no other CL will know of our proxies.
     */
    private class WeldProxyDeclaringCL extends ClassLoader {

        WeldProxyDeclaringCL(ClassLoader parent) {
            super(parent);
        }

        public final Class<?> publicDefineClass(String name, byte[] b, int off, int len) {
            try {
                // just being paranoid - try loading class first
                return super.loadClass(name);
            } catch (ClassNotFoundException e) {
                return super.defineClass(name, b, off, len);
            }
        }

        public final Class<?> publicDefineClass(String name, byte[] b, int off, int len, ProtectionDomain pd) {
            try {
                // just being paranoid - try loading class first
                return super.loadClass(name);
            } catch (ClassNotFoundException e) {
                return super.defineClass(name, b, off, len, pd);
            }
        }
    }
}
