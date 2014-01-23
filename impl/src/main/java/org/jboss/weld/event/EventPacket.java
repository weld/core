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
package org.jboss.weld.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.literal.AnyLiteral;

import com.google.common.collect.ImmutableSet;

/**
 * <p>
 * An event packet consisting of:
 * </p>
 *
 * <ul>
 * <li>event payload</li>
 * <li>event type</li>
 * <li>event qualifiers</li>
 * <li>{@link InjectionPoint} representing the injected {@link Event}</li>
 * </ul>
 *
 * @author Jozef Hartinger
 *
 */
public class EventPacket<T> implements EventMetadata {

    public static <T> EventPacket<T> of(T event, Type eventType, Set<Annotation> qualifiers, InjectionPoint ip) {
        return new EventPacket<T>(event, eventType, qualifiers, null, ip);
    }
    public static <T> EventPacket<T> of(T event, Annotation... qualifiers) {
        return new EventPacket<T>(event, event.getClass(), null, qualifiers, null);
    }

    private final T payload;
    private final Type type;
    private final InjectionPoint injectionPoint;

    private final Set<Annotation> qualifierSet;
    private final Annotation[] qualifierArray;

    private EventPacket(T payload, Type type, Set<Annotation> qualifierSet, Annotation[] qualifierArray, InjectionPoint injectionPoint) {
        this.payload = payload;
        this.type = type;
        this.qualifierSet = qualifierSet;
        this.qualifierArray = qualifierArray;
        this.injectionPoint = injectionPoint;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        ImmutableSet.Builder<Annotation> builder = ImmutableSet.<Annotation>builder();
        builder.add(AnyLiteral.INSTANCE);
        if (qualifierSet != null) {
            return builder.addAll(qualifierSet).build();
        } else if (qualifierArray != null) {
            return builder.add(qualifierArray).build();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    @Override
    public String toString() {
        return "EventPacket [payload=" + payload + ", type=" + type + ", qualifiers=" + getQualifiers() + "]";
    }
}
