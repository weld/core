/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.security.GetConstructorAction;
import org.jboss.weld.security.GetDeclaredFieldAction;
import org.jboss.weld.security.GetDeclaredFieldsAction;
import org.jboss.weld.security.SetAccessibleAction;

/**
 *
 * @author Martin Kouba
 */
final class SecurityActions {

    private SecurityActions() {
    }

    /**
     * Set the {@code accessible} flag for this accessible object. Does not perform {@link PrivilegedAction} unless necessary.
     *
     * @param accessibleObject
     */
    static void ensureAccessible(AccessibleObject accessibleObject) {
        if (accessibleObject != null) {
            if (!accessibleObject.isAccessible()) {
                if (System.getSecurityManager() != null) {
                    AccessController.doPrivileged(SetAccessibleAction.of(accessibleObject));
                } else {
                    accessibleObject.setAccessible(true);
                }
            }
        }
    }

    static <T> Constructor<T> getConstructor(Class<T> javaClass, Class<?>... parameterTypes) throws NoSuchMethodException {
        if (System.getSecurityManager() != null) {
            try {
                return AccessController.doPrivileged(GetConstructorAction.of(javaClass, parameterTypes));
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchMethodException) {
                    throw (NoSuchMethodException) e.getCause();
                }
                throw new WeldException(e.getCause());
            }
        } else {
            return javaClass.getConstructor(parameterTypes);
        }
    }

    static <T> Field getDeclaredField(Class<T> javaClass, String name) throws NoSuchFieldException {
        if (System.getSecurityManager() != null) {
            try {
                return AccessController.doPrivileged(new GetDeclaredFieldAction(javaClass, name));
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchFieldException) {
                    throw (NoSuchFieldException) e.getCause();
                }
                throw new WeldException(e.getCause());
            }
        } else {
            return javaClass.getDeclaredField(name);
        }
    }

    static boolean hasDeclaredField(Class<?> javaClass, String name) {
        Field[] fields;
        if (System.getSecurityManager() == null) {
            fields = javaClass.getDeclaredFields();
        } else {
            fields = AccessController.doPrivileged(new GetDeclaredFieldsAction(javaClass));
        }
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
