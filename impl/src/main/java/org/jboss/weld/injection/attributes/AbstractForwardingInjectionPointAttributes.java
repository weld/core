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

import java.io.Serializable;
import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.introspector.WeldAnnotated;

public abstract class AbstractForwardingInjectionPointAttributes<T, S> extends ForwardingInjectionPoint implements WeldInjectionPointAttributes<T, S>, Serializable {

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
    public abstract WeldAnnotated<T, S> getAnnotated();

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
