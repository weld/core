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
package org.jboss.weld.util.reflection;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.jboss.weld.util.Preconditions;

/**
 *
 * @author Martin Kouba
 */
public final class DeclaredMemberIndexer {

    private static final ConstructorComparator CONSTRUCTOR_COMPARATOR_INSTANCE = new ConstructorComparator();

    private static final MethodComparator METHOD_COMPARATOR_INSTANCE = new MethodComparator();

    private static final FieldComparator FIELD_COMPARATOR_INSTANCE = new FieldComparator();

    private DeclaredMemberIndexer() {
    }

    /**
     * @param field
     * @return the index for the given field
     */
    public static int getIndexForField(Field field) {
        Preconditions.checkNotNull(field);
        return getIndexForMember(field, getDeclaredFields(field.getDeclaringClass()));
    }

    /**
     * @param index
     * @param declaringClass
     * @return the declared field for the given index and declaring class
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public static Field getFieldForIndex(int index, Class<?> declaringClass) {
        return getDeclaredFields(declaringClass).get(index);
    }

    /**
     * @param method
     * @return the index for the given method
     */
    public static int getIndexForMethod(Method method) {
        Preconditions.checkNotNull(method);
        return getIndexForMember(method, getDeclaredMethods(method.getDeclaringClass()));
    }

    /**
     * @param index
     * @param declaringClass
     * @return the declared method for the given index and declaring class
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public static Method getMethodForIndex(int index, Class<?> declaringClass) {
        return getDeclaredMethods(declaringClass).get(index);
    }

    /**
     * @param constructor
     * @return the index for the given constructor
     */
    public static int getIndexForConstructor(Constructor<?> constructor) {
        Preconditions.checkNotNull(constructor);
        return getIndexForMember(constructor, getDeclaredConstructors(constructor.getDeclaringClass()));
    }

    /**
     * @param index
     * @param declaringClass
     * @return the declared constructor for the given index and declaring class
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public static <T> Constructor<T> getConstructorForIndex(int index, Class<T> declaringClass) {
        return cast(getDeclaredConstructors(declaringClass).get(index));
    }

    private static <T extends Member> int getIndexForMember(T declaredMember, List<T> declaredMembers) {

        for (ListIterator<T> iterator = declaredMembers.listIterator(); iterator.hasNext();) {
            T member = iterator.next();
            if (member.equals(declaredMember)) {
                return iterator.previousIndex();
            }
        }
        // This should never happen
        throw new IllegalStateException("No matching declared member found for: " + declaredMember);
    }

    /**
     * @param declaringClass
     * @return the ordered list of declared fields for the given class
     */
    public static List<Field> getDeclaredFields(Class<?> declaringClass) {
        Preconditions.checkNotNull(declaringClass);
        List<Field> declaredFields = Arrays.asList(declaringClass.getDeclaredFields());
        Collections.sort(declaredFields, FIELD_COMPARATOR_INSTANCE);
        return declaredFields;
    }

    /**
     * @param declaringClass
     * @return the ordered list of declared methods for the given class
     */
    public static List<Method> getDeclaredMethods(Class<?> declaringClass) {
        Preconditions.checkNotNull(declaringClass);
        List<Method> declaredMethods = Arrays.asList(declaringClass.getDeclaredMethods());
        Collections.sort(declaredMethods, METHOD_COMPARATOR_INSTANCE);
        return declaredMethods;
    }

    /**
     * @param declaringClass
     * @return the ordered list of declared constructors for the given class
     */
    public static List<Constructor<?>> getDeclaredConstructors(Class<?> declaringClass) {
        Preconditions.checkNotNull(declaringClass);
        List<Constructor<?>> declaredConstructors = Arrays.asList(declaringClass.getDeclaredConstructors());
        Collections.sort(declaredConstructors, CONSTRUCTOR_COMPARATOR_INSTANCE);
        return declaredConstructors;
    }

    private static int compareParamTypes(Class<?>[] paramTypes1, Class<?>[] paramTypes2) {

        // First compare the number of parameters
        if (paramTypes1.length != paramTypes2.length) {
            return paramTypes1.length - paramTypes2.length;
        }

        // Then compare the FCQNs of the parameter types
        for (int i = 0; i < paramTypes1.length; i++) {
            if (!paramTypes1[i].getName().equals(paramTypes2[i].getName())) {
                return paramTypes1[i].getName().compareTo(paramTypes2[i].getName());
            }
        }
        // This should never happen - constructors cannot have the same param
        // types and methods are ordered by name first
        return 0;
    }

    private static class ConstructorComparator implements Comparator<Constructor<?>>, Serializable {

        private static final long serialVersionUID = 4694814949925290433L;

        @Override
        public int compare(Constructor<?> c1, Constructor<?> c2) {
            return compareParamTypes(c1.getParameterTypes(), c2.getParameterTypes());
        }
    }

    private static class MethodComparator implements Comparator<Method>, Serializable {

        private static final long serialVersionUID = -2254993285161908832L;

        @Override
        public int compare(Method m1, Method m2) {
            // First compare the names
            if (!m1.getName().equals(m2.getName())) {
                return m1.getName().compareTo(m2.getName());
            }
            // Then compare the parameters
            return compareParamTypes(m1.getParameterTypes(), m2.getParameterTypes());
        }
    }

    private static class FieldComparator implements Comparator<Field>, Serializable {

        private static final long serialVersionUID = -1417596921060498760L;

        @Override
        public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
