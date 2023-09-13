/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.test.util.annotated;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * @author Stuart Douglas
 */
abstract class AbstractTestAnnotatedMember<X, M extends Member> extends AbstractTestAnnotatedElement
        implements AnnotatedMember<X> {
    private final AnnotatedType<X> declaringType;
    private final M javaMember;

    protected AbstractTestAnnotatedMember(AnnotatedType<X> declaringType, M member, Class<?> memberType,
            TestAnnotationStore annotations) {
        super(memberType, annotations);
        this.declaringType = declaringType;
        this.javaMember = member;
    }

    public AnnotatedType<X> getDeclaringType() {
        return declaringType;
    }

    public M getJavaMember() {
        return javaMember;
    }

    public boolean isStatic() {
        return Modifier.isStatic(javaMember.getModifiers());
    }

}
