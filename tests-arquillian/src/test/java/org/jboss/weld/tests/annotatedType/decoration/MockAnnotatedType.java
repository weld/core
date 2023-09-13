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
package org.jboss.weld.tests.annotatedType.decoration;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
class MockAnnotatedType<X> extends MockAnnotated implements AnnotatedType<X> {
    private final Set<AnnotatedConstructor<X>> annotatedConstructors;

    private final Set<AnnotatedField<? super X>> annotatedFields;

    private final Set<AnnotatedMethod<? super X>> annotatedMethods;

    MockAnnotatedType(AnnotatedType<X> delegate) {
        super(delegate);
        annotatedConstructors = initialiseConstructors();
        annotatedFields = initialiseAnnotatedFields();
        annotatedMethods = initialiseMethods();
    }

    private Set<AnnotatedField<? super X>> initialiseAnnotatedFields() {
        Set<AnnotatedField<? super X>> fields = new HashSet<AnnotatedField<? super X>>();
        for (AnnotatedField<? super X> field : getDelegate().getFields()) {
            if (field.isStatic()) {
                fields.add(field);
            } else {
                fields.add(new MockAnnotatedField<X>(field));
            }
        }
        return fields;
    }

    private Set<AnnotatedConstructor<X>> initialiseConstructors() {
        Set<AnnotatedConstructor<X>> constructors = new HashSet<AnnotatedConstructor<X>>();
        for (AnnotatedConstructor<X> constructor : getDelegate().getConstructors()) {
            constructors.add(new MockAnnotatedConstructor<X>(constructor));
        }
        return constructors;
    }

    private Set<AnnotatedMethod<? super X>> initialiseMethods() {
        Set<AnnotatedMethod<? super X>> methods = new HashSet<AnnotatedMethod<? super X>>();
        for (AnnotatedMethod<? super X> method : getDelegate().getMethods()) {
            if (method.isStatic()) {
                methods.add(method);
            } else {
                methods.add(new MockAnnotatedMethod<X>(method));
            }
        }
        return methods;
    }

    @Override
    AnnotatedType<X> getDelegate() {
        return (AnnotatedType<X>) super.getDelegate();
    }

    public Set<AnnotatedConstructor<X>> getConstructors() {
        return annotatedConstructors;
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return annotatedFields;
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return annotatedMethods;
    }

    public Class<X> getJavaClass() {
        return getDelegate().getJavaClass();
    }
}
