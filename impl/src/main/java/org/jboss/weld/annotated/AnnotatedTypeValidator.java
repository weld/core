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

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.MetadataMessage.INVALID_PARAMETER_POSITION;
import static org.jboss.weld.logging.messages.MetadataMessage.METADATA_SOURCE_RETURNED_NULL;
import static org.jboss.weld.logging.messages.MetadataMessage.NO_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.MetadataMessage.NOT_IN_HIERARCHY;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.slf4j.cal10n.LocLogger;

/**
 * Validates that methods of an {@link Annotated} implementation return sane values.
 * @author Jozef Hartinger
 *
 */
public class AnnotatedTypeValidator {
    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

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
            throw new IllegalArgumentException(INVALID_PARAMETER_POSITION, parameter.getPosition(), parameter);
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
        checkSensibility(type);
        checkNotNull(type.getJavaClass(), "getJavaClass()", type);
        checkNotNull(type.getFields(), "getFields()", type);
        checkNotNull(type.getConstructors(), "getConstructors()", type);
        checkNotNull(type.getMethods(), "getMethods()", type);
    }

    private static void checkNotNull(Object expression, String methodName, Object target) {
        if (expression == null) {
            throw new IllegalArgumentException(METADATA_SOURCE_RETURNED_NULL, methodName, target);
        }
    }

    private static void checkSensibility(AnnotatedType<?> type) {
        //check if it has a constructor
        if(type.getConstructors().isEmpty()) {
            log.warn(NO_CONSTRUCTOR,type);
        }
        //check if its javaMembers belong to the class hierarchy of annotatedType
        List<AnnotatedMember<?>> members = new ArrayList<AnnotatedMember<?>>();
        members.addAll(type.getConstructors());
        members.addAll(type.getFields());
        members.addAll(type.getMethods());
        Set<Type> typeClosures = type.getTypeClosure();
        for(AnnotatedMember<?> member: members) {
           if(!typeClosures.contains(member.getJavaMember().getDeclaringClass())) {
                log.warn(NOT_IN_HIERARCHY,member.toString(),type.toString());
           }
        }
    }

}
