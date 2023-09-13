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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;

public class GetAccessibleCopyOfMember<T extends AccessibleObject & Member> implements PrivilegedAction<T> {

    private static final String UNABLE_TO_OBTAIN_AN_ACCESSIBLE_COPY_OF = "Unable to obtain an accessible copy of ";

    private final T originalMember;

    public GetAccessibleCopyOfMember(T originalMember) {
        this.originalMember = originalMember;
    }

    public static <T extends AccessibleObject & Member> T of(T member) {
        T copy = copyMember(member);
        copy.setAccessible(true);
        return copy;
    }

    @Override
    public T run() {
        return of(originalMember);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AccessibleObject & Member> T copyMember(T originalMember) {
        Class<?> declaringClass = originalMember.getDeclaringClass();
        try {
            if (originalMember instanceof Field) {
                return (T) copyField((Field) originalMember, declaringClass);
            }
            if (originalMember instanceof Constructor<?>) {
                return (T) copyConstructor((Constructor<?>) originalMember, declaringClass);
            }
            if (originalMember instanceof Method) {
                return (T) copyMethod((Method) originalMember, declaringClass);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(UNABLE_TO_OBTAIN_AN_ACCESSIBLE_COPY_OF + originalMember, e);
        }
        throw new IllegalArgumentException(UNABLE_TO_OBTAIN_AN_ACCESSIBLE_COPY_OF + originalMember);
    }

    private static Field copyField(Field field, Class<?> declaringClass) throws NoSuchFieldException {
        return declaringClass.getDeclaredField(field.getName());
    }

    private static Method copyMethod(Method method, Class<?> declaringClass) throws NoSuchMethodException {
        return declaringClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
    }

    private static Constructor<?> copyConstructor(Constructor<?> constructor, Class<?> declaringClass)
            throws NoSuchMethodException {
        return declaringClass.getDeclaredConstructor(constructor.getParameterTypes());
    }
}
