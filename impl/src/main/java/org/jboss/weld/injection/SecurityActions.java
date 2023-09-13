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
package org.jboss.weld.injection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.security.GetAccessibleCopyOfMember;
import org.jboss.weld.security.MethodLookupAction;
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

    /**
     * Does not perform {@link PrivilegedAction} unless necessary.
     *
     * @param javaClass
     * @param methodName
     * @param parameterTypes
     * @return returns a method from the class or any class/interface in the inheritance hierarchy
     * @throws NoSuchMethodException
     */
    static Method lookupMethod(Class<?> javaClass, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (System.getSecurityManager() != null) {
            try {
                return AccessController.doPrivileged(new MethodLookupAction(javaClass, methodName, parameterTypes));
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchMethodException) {
                    throw (NoSuchMethodException) e.getCause();
                }
                throw new WeldException(e.getCause());
            }
        } else {
            return MethodLookupAction.lookupMethod(javaClass, methodName, parameterTypes);
        }
    }

    static Method getAccessibleCopyOfMethod(Method method) {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new GetAccessibleCopyOfMember<Method>(method));
        } else {
            return GetAccessibleCopyOfMember.of(method);
        }
    }
}
