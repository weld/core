/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.security;

import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

/**
 * Returns a method from the class or any class/interface in the inheritance hierarchy
 *
 * @author Jozef Hartinger
 *
 */
public class MethodLookupAction extends GetDeclaredMethodAction implements PrivilegedExceptionAction<Method> {

    public MethodLookupAction(Class<?> javaClass, String methodName, Class<?>[] parameterTypes) {
        super(javaClass, methodName, parameterTypes);
    }

    @Override
    public Method run() throws NoSuchMethodException {
        return lookupMethod(javaClass, methodName, parameterTypes);
    }

    public static Method lookupMethod(Class<?> javaClass, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> inspectedClass = javaClass; inspectedClass != null; inspectedClass = inspectedClass.getSuperclass()) {
            for (Class<?> inspectedInterface : inspectedClass.getInterfaces()) {
                try {
                    return lookupMethod(inspectedInterface, methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    // Expected, nothing to see here.
                }
            }
            try {
                return inspectedClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException nsme) {
                // Expected, nothing to see here.
            }
        }
        throw new NoSuchMethodException(
                javaClass + ", method: " + methodName + ", paramTypes: " + Arrays.toString(parameterTypes));
    }
}
