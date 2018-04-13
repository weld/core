/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import org.jboss.classfilewriter.ClassFile;

import sun.misc.Unsafe;

/**
 * Utility class for loading a ClassFile into a classloader. This borrows heavily from javassist
 *
 * In oder to support JDK 9+, this class now uses Unsafe as we need to be able to define classes with different ProtectionDomain
 *
 * @author Stuart Douglas
 * @author Matej Novotny
 */
public class ClassFileUtils {

    private static java.lang.reflect.Method defineClass1, defineClass2;

    private ClassFileUtils() {
    }

    static {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Class<?> cl = Class.forName("java.lang.ClassLoader");
                    final String name = "defineClass";

                    // get Unsafe singleton instance
                    Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
                    singleoneInstanceField.setAccessible(true);
                    Unsafe theUnsafe = (Unsafe) singleoneInstanceField.get(null);

                    // get the offset of the override field in AccessibleObject
                    long overrideOffset = theUnsafe.objectFieldOffset(AccessibleObject.class.getDeclaredField("override"));

                    defineClass1 = cl.getDeclaredMethod(name, new Class[] { String.class, byte[].class, int.class, int.class });
                    defineClass2 = cl.getDeclaredMethod(name, new Class[] { String.class, byte[].class, int.class, int.class, ProtectionDomain.class });

                    // make both accessible
                    theUnsafe.putBoolean(defineClass1, overrideOffset, true);
                    theUnsafe.putBoolean(defineClass2, overrideOffset, true);
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException("cannot initialize ClassPool", pae.getException());
        }
    }

    /**
     * Converts the class to a <code>java.lang.Class</code> object. Once this method is called, further modifications are not
     * allowed any more.
     * <p/>
     * <p/>
     * The class file represented by the given <code>CtClass</code> is loaded by the given class loader to construct a
     * <code>java.lang.Class</code> object. Since a private method on the class loader is invoked through the reflection API,
     * the caller must have permissions to do that.
     * <p/>
     * <p/>
     * An easy way to obtain <code>ProtectionDomain</code> object is to call <code>getProtectionDomain()</code> in
     * <code>java.lang.Class</code>. It returns the domain that the class belongs to.
     * <p/>
     * <p/>
     * This method is provided for convenience. If you need more complex functionality, you should write your own class loader.
     *
     * @param loader the class loader used to load this class. For example, the loader returned by <code>getClassLoader()</code>
     *               can be used for this parameter.
     * @param domain the protection domain for the class. If it is null, the default domain created by
     *               <code>java.lang.ClassLoader</code> is
     */
    public static Class<?> toClass(ClassFile ct, ClassLoader loader, ProtectionDomain domain) {
        try {
            byte[] b = ct.toBytecode();
            java.lang.reflect.Method method;
            Object[] args;
            if (domain == null) {
                method = defineClass1;
                args = new Object[] { ct.getName(), b, 0, b.length };
            } else {
                method = defineClass2;
                args = new Object[] { ct.getName(), b, 0, b.length, domain };
            }

            return toClass2(method, loader, args);
        } catch (RuntimeException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static synchronized Class<?> toClass2(Method method, ClassLoader loader, Object[] args) throws Exception {
        Class<?> clazz = Class.class.cast(method.invoke(loader, args));
        return clazz;
    }

}
