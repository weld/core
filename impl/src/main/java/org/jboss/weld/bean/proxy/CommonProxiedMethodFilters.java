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
package org.jboss.weld.bean.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.jboss.weld.util.reflection.Reflections;

public final class CommonProxiedMethodFilters {

    private CommonProxiedMethodFilters() {
    }

    public static final ProxiedMethodFilter NON_STATIC = (m, c) -> !Modifier.isStatic(m.getModifiers());

    public static final ProxiedMethodFilter NON_FINAL = (m, c) -> !Modifier.isFinal(m.getModifiers());

    public static final ProxiedMethodFilter OBJECT_TO_STRING = (m, c) -> m.getDeclaringClass() != Object.class
            || m.getName().equals("toString");

    public static final ProxiedMethodFilter NON_PRIVATE = (m, c) -> !Modifier.isPrivate(m.getModifiers());

    /**
     * For JDK classes do not accept any package-private method and/or method with package-private parameter types. A generated
     * class is not allowed to use a
     * package name starting with the identifier <code>java</code>.
     */
    public static final ProxiedMethodFilter NON_JDK_PACKAGE_PRIVATE = new ProxiedMethodFilter() {

        @Override
        public boolean accept(Method method, Class<?> proxySuperclass) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass != null) {
                Package pack = declaringClass.getPackage();
                if (pack != null && pack.getName().startsWith(ProxyFactory.JAVA)) {
                    if (Reflections.isPackagePrivate(method.getModifiers())) {
                        return false;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (Class<?> parameterType : parameterTypes) {
                        if (Reflections.isPackagePrivate(parameterType.getModifiers())) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    };

    /**
     * Filter used to exclude private methods that have parameters which are package private types
     */
    public static final ProxiedMethodFilter NON_PRIVATE_WITHOUT_PACK_PRIVATE_PARAMS = new ProxiedMethodFilter() {
        @Override
        public boolean accept(Method method, Class<?> proxySuperclass) {
            if (Modifier.isPrivate(method.getModifiers())) {
                for (Parameter param : method.getParameters()) {
                    Class<?> paramClass = param.getType();
                    if (!Modifier.isProtected(paramClass.getModifiers())
                            && !Modifier.isPublic(paramClass.getModifiers())
                            && !Modifier.isPrivate(paramClass.getModifiers())) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
}
