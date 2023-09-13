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
package org.jboss.weld.injection.attributes;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.ForwardingInjectionPoint;

public abstract class AbstractForwardingInjectionPointAttributes<T, S> extends ForwardingInjectionPoint
        implements WeldInjectionPointAttributes<T, S>, Serializable {

    private static final long serialVersionUID = -7540261474875045335L;

    // the delegate is assumed to be serializable
    private final InjectionPoint delegate;

    public AbstractForwardingInjectionPointAttributes(InjectionPoint delegate) {
        this.delegate = delegate;
    }

    @Override
    protected InjectionPoint delegate() {
        return delegate;
    }

    @Override
    public <A extends Annotation> A getQualifier(Class<A> annotationType) {
        for (Annotation qualifier : getQualifiers()) {
            if (qualifier.annotationType().equals(annotationType)) {
                return annotationType.cast(qualifier);
            }
        }
        return null;
    }
}
