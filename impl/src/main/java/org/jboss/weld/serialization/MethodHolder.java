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

import java.lang.reflect.Method;
import java.util.Objects;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.util.reflection.DeclaredMemberIndexer;

/**
 * Serializable holder for {@link Method}.
 *
 * @author Jozef Hartinger
 *
 */
public class MethodHolder extends AbstractSerializableHolder<Method> {

    private static final long serialVersionUID = -3033089710155551280L;

    private final Class<?> declaringClass;
    private final int index;

    public MethodHolder(Method method) {
        super(method);
        this.index = DeclaredMemberIndexer.getIndexForMethod(method);
        this.declaringClass = method.getDeclaringClass();
    }

    public static MethodHolder of(Method method) {
        return new MethodHolder(method);
    }

    public static MethodHolder of(AnnotatedMethod<?> method) {
        return new MethodHolder(method.getJavaMember());
    }

    @Override
    protected Method initialize() {
        try {
            return DeclaredMemberIndexer.getMethodForIndex(index, declaringClass);
        } catch (Exception e) {
            throw ReflectionLogger.LOG.unableToGetMethodOnDeserialization(declaringClass, index, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodHolder that = (MethodHolder) o;
        return Objects.equals(get(), that.get());
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

}
