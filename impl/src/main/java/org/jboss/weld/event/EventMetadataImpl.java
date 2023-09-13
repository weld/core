/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Simple {@link EventMetadata} implementation.
 *
 * @author Jozef Hartinger
 *
 */
public final class EventMetadataImpl implements EventMetadata {

    private final Type type;
    private final InjectionPoint injectionPoint;
    private final Set<Annotation> qualifiers;
    private final Annotation[] qualifierArray;

    public EventMetadataImpl(Type type, InjectionPoint injectionPoint, Set<Annotation> qualifiers) {
        this(type, injectionPoint, qualifiers, null);
    }

    public EventMetadataImpl(Type type, InjectionPoint injectionPoint, Annotation[] qualifiers) {
        this(type, injectionPoint, null, qualifiers);
    }

    private EventMetadataImpl(Type type, InjectionPoint injectionPoint, Set<Annotation> qualifiers,
            Annotation[] qualifierArray) {
        this.type = type;
        this.injectionPoint = injectionPoint;
        this.qualifiers = qualifiers;
        this.qualifierArray = qualifierArray;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        ImmutableSet.Builder<Annotation> builder = ImmutableSet.<Annotation> builder();
        builder.add(Any.Literal.INSTANCE);
        if (qualifiers != null) {
            return builder.addAll(qualifiers).build();
        } else if (qualifierArray != null) {
            return builder.addAll(qualifierArray).build();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "EventMetadataImpl [type=" + type + ", qualifiers=" + qualifiers + ", injectionPoint=" + injectionPoint + "]";
    }
}
