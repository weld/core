/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.injection.attributes;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that forwards calls to an extension-provided {@link InjectionPoint}
 * implementation.
 *
 * @author Jozef Hartinger
 *
 */
public class ForwardingFieldInjectionPointAttributes<T, X> extends AbstractForwardingInjectionPointAttributes<T, Field> implements FieldInjectionPointAttributes<T, X> {

    public static <T, X> FieldInjectionPointAttributes<T, X> of(InjectionPoint ip) {
        if (ip instanceof FieldInjectionPointAttributes<?, ?>) {
            return Reflections.cast(ip);
        }
        if (!(ip.getAnnotated() instanceof AnnotatedField<?>)) {
            throw new IllegalArgumentException(BeanMessage.INVALID_INJECTION_POINT_TYPE, ForwardingFieldInjectionPointAttributes.class, ip.getAnnotated());
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
}
