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
package org.jboss.weld.annotated.runtime;

import static org.jboss.weld.logging.messages.UtilMessage.ACCESS_ERROR_ON_FIELD;

import java.lang.reflect.InvocationTargetException;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Utility methods for operating on {@link AnnotatedMember}s at runtime.
 *
 * @author Jozef Hartinger
 *
 */
public class RuntimeAnnotatedMembers {

    /**
     * Creates a new instance
     *
     * @param parameters the parameters
     * @return An instance
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor#newInstance(Object... params)
     */
    public static <T> T newInstance(AnnotatedConstructor<T> constructor, Object... parameters) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return SecureReflections.ensureAccessible(constructor.getJavaMember()).newInstance(parameters);
    }

    /**
     * Injects an instance
     *
     * @param declaringInstance The instance to inject into
     * @param value             The value to inject
     */
    public static void setFieldValue(AnnotatedField<?> field, Object instance, Object value) throws IllegalArgumentException, IllegalAccessException {
        SecureReflections.ensureAccessible(field.getJavaMember()).set(instance, value);
    }

    public static void setFieldValueOnInstance(AnnotatedField<?> field, Object instance, Object value) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        SecureReflections.getField(instance.getClass(), field.getJavaMember().getName()).set(instance, value);
    }

    public static <T, X> T getFieldValue(AnnotatedField<X> field, Object instance) {
        try {
            return Reflections.<T>cast(SecureReflections.ensureAccessible(field.getJavaMember()).get(instance));
        } catch (Exception e) {
            throw new WeldException(ACCESS_ERROR_ON_FIELD, e, field.getJavaMember().getName(), field.getJavaMember().getDeclaringClass());
        }
    }

    public static <T, X> T invokeMethod(AnnotatedMethod<X> method, Object instance, Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return SecureReflections.<T>invoke(instance, method.getJavaMember(), parameters);
    }
}
