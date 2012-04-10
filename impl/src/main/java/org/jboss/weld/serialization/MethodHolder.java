/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.serialization;

import static org.jboss.weld.logging.messages.ReflectionMessage.UNABLE_TO_GET_METHOD_ON_DESERIALIZATION;

import java.lang.reflect.Method;

import org.jboss.weld.exceptions.WeldException;

/**
 * Serializable holder for {@link Method}.
 *
 * @author Jozef Hartinger
 *
 */
public class MethodHolder extends AbstractSerializableHolder<Method> {

    private static final long serialVersionUID = -3033089710155551280L;

    private final Class<?> declaringClass;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    public MethodHolder(Method method) {
        super(method);
        this.declaringClass = method.getDeclaringClass();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
    }

    @Override
    protected Method initialize() {
        try {
            return declaringClass.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            throw new WeldException(UNABLE_TO_GET_METHOD_ON_DESERIALIZATION, e, declaringClass, parameterTypes);
        }
    }
}
