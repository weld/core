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
package org.jboss.weld.annotated;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Validates that methods of an {@link Annotated} implementation return sane values.
 *
 * @author Jozef Hartinger
 *
 */
public class AnnotatedTypeValidator {

    private AnnotatedTypeValidator() {
    }

    public static void validateAnnotated(Annotated annotated) {
        checkNotNull(annotated.getAnnotations(), "getAnnotations()", annotated);
        checkNotNull(annotated.getBaseType(), "getBaseType()", annotated);
        checkNotNull(annotated.getTypeClosure(), "getTypeClosure()", annotated);
    }

    public static void validateAnnotatedParameter(AnnotatedParameter<?> parameter) {
        validateAnnotated(parameter);
        if (parameter.getPosition() < 0) {
            throw MetadataLogger.LOG.invalidParameterPosition(parameter.getPosition(), parameter);
        }
        checkNotNull(parameter.getDeclaringCallable(), "getDeclaringCallable()", parameter);
    }

    public static void validateAnnotatedMember(AnnotatedMember<?> member) {
        validateAnnotated(member);
        checkNotNull(member.getJavaMember(), "getJavaMember()", member);
        checkNotNull(member.getDeclaringType(), "getDeclaringType()", member);
    }

    public static void validateAnnotatedType(AnnotatedType<?> type) {
        validateAnnotated(type);
        checkNotNull(type.getJavaClass(), "getJavaClass()", type);
        checkNotNull(type.getFields(), "getFields()", type);
        checkNotNull(type.getConstructors(), "getConstructors()", type);
        checkNotNull(type.getMethods(), "getMethods()", type);
        checkSensibility(type);
    }

    private static void checkNotNull(Object expression, String methodName, Object target) {
        if (expression == null) {
            throw MetadataLogger.LOG.metadataSourceReturnedNull(methodName, target);
        }
    }

    /**
     * Checks if the given AnnotatedType is sensible, otherwise provides warnings.
     */
    private static void checkSensibility(AnnotatedType<?> type) {
        // check if it has a constructor
        if (type.getConstructors().isEmpty() && !type.getJavaClass().isInterface()) {
            MetadataLogger.LOG.noConstructor(type);
        }

        Set<Class<?>> hierarchy = new HashSet<Class<?>>();
        for (Class<?> clazz = type.getJavaClass(); clazz != null; clazz = clazz.getSuperclass()) {
            hierarchy.add(clazz);
            hierarchy.addAll(Reflections.getInterfaceClosure(clazz));
        }
        checkMembersBelongToHierarchy(type.getConstructors(), hierarchy, type);
        checkMembersBelongToHierarchy(type.getMethods(), hierarchy, type);
        checkMembersBelongToHierarchy(type.getFields(), hierarchy, type);
    }

    private static void checkMembersBelongToHierarchy(Iterable<? extends AnnotatedMember<?>> members, Set<Class<?>> hierarchy,
            AnnotatedType<?> type) {
        for (AnnotatedMember<?> member : members) {
            if (!hierarchy.contains(member.getJavaMember().getDeclaringClass())) {
                MetadataLogger.LOG.notInHierarchy(member.getJavaMember().getName(), member.toString(),
                        type.getJavaClass().getName(), type.toString(),
                        Formats.formatAsStackTraceElement(member.getJavaMember()));
            }
        }
    }

}
