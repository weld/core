/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author David Allen
 */
public abstract class AbstractBeanInstance implements BeanInstance {

    public Object invoke(Object instance, Method method, Object... arguments) throws Throwable {
        Object result = null;
        try {
            Reflections.ensureAccessible(method, instance);
            result = method.invoke(instance, arguments);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

    protected Class<?> computeInstanceType(Bean<?> bean) {
        return computeInstanceType(bean.getTypes());
    }

    protected static Class<?> computeInstanceType(Set<Type> types) {
        TypeInfo typeInfo = TypeInfo.of(types);
        Class<?> superClass = typeInfo.getSuperClass();
        if (superClass.equals(Object.class)) {
            superClass = typeInfo.getSuperInterface();
        }
        return superClass;
    }
}
