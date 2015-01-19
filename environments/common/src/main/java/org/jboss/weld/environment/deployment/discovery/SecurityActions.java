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
package org.jboss.weld.environment.deployment.discovery;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.security.GetConstructorAction;

/**
 *
 * @author Martin Kouba
 */
final class SecurityActions {

    private SecurityActions() {
    }

    /**
     * Does not perform {@link PrivilegedAction} unless necessary.
     *
     * @param javaClass
     * @param parameterTypes
     * @return a declared constructor
     * @throws NoSuchMethodException
     */
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

}
