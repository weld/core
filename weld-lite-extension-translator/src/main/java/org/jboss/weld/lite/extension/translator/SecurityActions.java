/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.lite.extension.translator;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.security.GetConstructorAction;
import org.jboss.weld.security.GetDeclaredConstructorsAction;
import org.jboss.weld.security.GetDeclaredFieldsAction;
import org.jboss.weld.security.GetDeclaredMethodsAction;
import org.jboss.weld.security.SetAccessibleAction;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author Matej Novotny
 */
final class SecurityActions {

    private SecurityActions() {
    }

    /**
     * Set the {@code accessible} flag for this accessible object. Does not perform {@link PrivilegedAction} unless necessary.
     */
    static void ensureAccessible(AccessibleObject accessibleObject, Object declaringClassObject) {
        if (accessibleObject != null) {
            if (!accessibleObject.canAccess(declaringClassObject)) {
                if (System.getSecurityManager() != null) {
                    AccessController.doPrivileged(SetAccessibleAction.of(accessibleObject));
                } else {
                    accessibleObject.setAccessible(true);
                }
            }
        }
    }

    static <T> Constructor<T> getConstructor(Class<T> javaClass) throws NoSuchMethodException {
        if (System.getSecurityManager() != null) {
            try {
                return AccessController.doPrivileged(GetConstructorAction.of(javaClass));
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchMethodException) {
                    throw (NoSuchMethodException) e.getCause();
                }
                throw new WeldException(e.getCause());
            }
        } else {
            return javaClass.getConstructor();
        }
    }

    static <T> Constructor<T>[] getDeclaredConstructors(Class<T> javaClass) {
        if (System.getSecurityManager() != null) {
            return Reflections.cast(AccessController.doPrivileged(new GetDeclaredConstructorsAction(javaClass)));
        } else {
            return Reflections.cast(javaClass.getDeclaredConstructors());
        }
    }

    static Method[] getDeclaredMethods(Class<?> javaClass) {
        if (System.getSecurityManager() != null) {
            return Reflections.cast(AccessController.doPrivileged(new GetDeclaredMethodsAction(javaClass)));
        } else {
            return Reflections.cast(javaClass.getDeclaredMethods());
        }
    }

    static Field[] getDeclaredFields(Class<?> javaClass) {
        if (System.getSecurityManager() != null) {
            return Reflections.cast(AccessController.doPrivileged(new GetDeclaredFieldsAction(javaClass)));
        } else {
            return Reflections.cast(javaClass.getDeclaredFields());
        }
    }

}
