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
package org.jboss.weld.environment.deployment.discovery.jandex;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.security.ConstructorNewInstanceAction;
import org.jboss.weld.security.NewInstanceAction;

/**
 * @author Martin Kouba
 */
final class SecurityActions {

    private SecurityActions() {
    }

    /**
     * @param javaClass
     * @return a new instance of the given class
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    static <T> T newInstance(Class<T> javaClass) throws InstantiationException, IllegalAccessException {
        if (System.getSecurityManager() != null) {
            try {
                return AccessController.doPrivileged(NewInstanceAction.of(javaClass));
            } catch (PrivilegedActionException e) {
                throw new WeldException(e.getCause());
            }
        } else {
            return javaClass.newInstance();
        }
    }

    /**
     * @param javaClass
     * @param types
     * @param params
     * @param <T>
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    static <T> T newConstructorInstance(Class<T> javaClass, Class<?>[] constructorParamTypes,
            Object... constructorParamInstances)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (System.getSecurityManager() != null) {
            try {
                return AccessController.doPrivileged(
                        ConstructorNewInstanceAction.of(javaClass, constructorParamTypes, constructorParamInstances));
            } catch (PrivilegedActionException e) {
                throw new WeldException(e.getCause());
            }
        } else {
            return javaClass.getConstructor(constructorParamTypes).newInstance(constructorParamInstances);
        }
    }

}
