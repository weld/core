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

package org.jboss.weld.util.bytecode;

import org.jboss.classfilewriter.ClassFile;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bootstrap.WeldStartup;
import org.jboss.weld.serialization.spi.ProxyServices;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * JDK 11 - friendly variant that avoids breaking into CLs and instead uses {@code MethodHandles.Lookup} if possible.
 * For proxies of Java internal classes (packages {@code java.*} and {@code javax.*}) and for proxies of classes inside
 * signed JARs, we have to use our own class loader. See {@link WeldProxyDeclaringCL}
 *
 * @author Matej Novotny
 */
public class ClassFileUtils {

    private ClassFileUtils() {
    }

    /**
     * Noop under JDK 11+
     */
    public static void makeClassLoaderMethodsAccessible() {
        // noop, in newer JDK, we no longer need to perform this
    }

    /**
     * Allows to define a new class using either {@link MethodHandles.Lookup} or a wrapping class loader
     * ({@link WeldProxyDeclaringCL}). If possible, we will use {@link MethodHandles.Lookup} but if the new class needs
     * to be created in non-existent package (proxies for java.* etc), we are forced to use class loader approach.
     *
     *
     * @param ct {@code ClassFile} to be defined
     * @param loader class loader which was used to declare the original class, used for wrapping it with our CL
     * @param domain protection domain of the class; only matters if we use the CL-approach
     * @param originalClass base class on top of which we built the proxy (or rather, the {@code ClassFile})
     * @return
     */
    public static Class<?> toClass(ClassFile ct, ClassLoader loader, ProtectionDomain domain, Class<?> originalClass) {
        try {
            String classToDefineName = ct.getName();
            byte[] classBytes = ct.toBytecode();
            // this is one of the classes we define into our own packages, we need to use a CL approach
            if (classToDefineName.startsWith(ProxyFactory.WELD_PROXY_PREFIX)) {
                return defineWithClassLoader(classToDefineName, classBytes, classBytes.length, loader, domain);
            } else {
                // these classes go into existing packages, we will use MethodHandles to define them
                return defineWithMethodLookup(classToDefineName, classBytes, originalClass, loader);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    private static Class<?> defineWithClassLoader(String classToDefineName, byte[] classBytes, int length, ClassLoader loader, ProtectionDomain domain) {
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
    private static WeldProxyDeclaringCL returnWeldCL(ClassLoader loader) {
            if (loader instanceof WeldProxyDeclaringCL) {
                return (WeldProxyDeclaringCL) loader;
            }
            return new WeldProxyDeclaringCL(loader);
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
    private static Class<?> defineWithMethodLookup(String classToDefineName, byte[] classBytes, Class<?> originalClass, ClassLoader loader) {
        Module thisModule = ClassFileUtils.class.getModule();
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
     * Delegates proxy creation via {@link ProxyServices} to the integrator.
     */
    public static Class<?> toClass(ClassFile ct, Class<?> originalClass, ProxyServices proxyServices, ProtectionDomain domain) {
        try {
            byte[] bytecode = ct.toBytecode();
            Class<?> result;
            if (domain == null) {
                result = proxyServices.defineClass(originalClass, ct.getName(), bytecode, 0, bytecode.length);
            } else {
                result = proxyServices.defineClass(originalClass, ct.getName(), bytecode, 0, bytecode.length, domain);
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
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
    private static class WeldProxyDeclaringCL extends ClassLoader {


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

