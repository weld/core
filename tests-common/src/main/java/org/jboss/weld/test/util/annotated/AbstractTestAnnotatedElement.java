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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;

/**
 * The base class for all New Annotated types.
 *
 * @author Stuart Douglas
 */
abstract class AbstractTestAnnotatedElement implements Annotated {

    private final Class<?> type;
    private final Set<Type> typeClosure;
    private final TestAnnotationStore annotations;

    protected AbstractTestAnnotatedElement(Class<?> type, TestAnnotationStore annotations) {
        this.typeClosure = new TestTypeClosureBuilder().add(type).getTypes();
        if (annotations == null) {
            this.annotations = new TestAnnotationStore();
        } else {
            this.annotations = annotations;
        }
        this.type = type;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return annotations.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return annotations.getAnnotations();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return annotations.isAnnotationPresent(annotationType);
    }

    public Set<Type> getTypeClosure() {
        return Collections.unmodifiableSet(typeClosure);
    }

    public Type getBaseType() {
        return type;
    }

}
