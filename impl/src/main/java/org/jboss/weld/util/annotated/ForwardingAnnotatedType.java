/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.annotated;

import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * Forwarding implementation of AnnotatedType
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * @author Ales Justin
 */
public abstract class ForwardingAnnotatedType<X> extends ForwardingAnnotated implements AnnotatedType<X> {

    @Override
    public abstract AnnotatedType<X> delegate();

    public Set<AnnotatedConstructor<X>> getConstructors() {
        return delegate().getConstructors();
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return delegate().getFields();
    }

    public Class<X> getJavaClass() {
        return delegate().getJavaClass();
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return delegate().getMethods();
    }

}
