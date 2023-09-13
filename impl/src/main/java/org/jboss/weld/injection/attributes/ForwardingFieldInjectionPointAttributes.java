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

import java.lang.reflect.Field;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that forwards calls to an extension-provided {@link InjectionPoint}
 * implementation.
 *
 * @author Jozef Hartinger
 *
 */
public class ForwardingFieldInjectionPointAttributes<T, X> extends AbstractForwardingInjectionPointAttributes<T, Field>
        implements FieldInjectionPointAttributes<T, X> {

    public static <T, X> FieldInjectionPointAttributes<T, X> of(InjectionPoint ip) {
        if (ip instanceof FieldInjectionPointAttributes<?, ?>) {
            return Reflections.cast(ip);
        }
        if (!(ip.getAnnotated() instanceof AnnotatedField<?>) || !(ip.getMember() instanceof Field)) {
            throw BeanLogger.LOG.invalidInjectionPointType(ForwardingFieldInjectionPointAttributes.class, ip.getAnnotated());
        }
        return new ForwardingFieldInjectionPointAttributes<T, X>(ip);
    }

    private static final long serialVersionUID = 427326058103802742L;

    protected ForwardingFieldInjectionPointAttributes(InjectionPoint delegate) {
        super(delegate);
    }

    @Override
    public AnnotatedField<X> getAnnotated() {
        return Reflections.cast(delegate().getAnnotated()); // checked in initializer
    }

    @Override
    public Field getMember() {
        return (Field) delegate().getMember();
    }
}
