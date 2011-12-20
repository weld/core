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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Represents a method/constructor parameter, which is not an injection point. This can be either a disposed or event parameter.
 *
 * @author Jozef Hartinger
 *
 */
public class SpecialParameterInjectionPoint<T, X> extends ForwardingInjectionPointAttributes<T, Object> implements ParameterInjectionPoint<T, X> {

    public static <T, X> ParameterInjectionPoint<T, X> of(WeldParameter<T, X> parameter, Bean<?> bean) {
        return new SpecialParameterInjectionPoint<T, X>(parameter, bean);
    }

    private final ParameterInjectionPointAttributes<T, X> attributes;

    protected SpecialParameterInjectionPoint(WeldParameter<T, X> parameter, Bean<?> bean) {
        this.attributes = InferingParameterInjectionPointAttributes.of(parameter, bean);
    }

    @Override
    public void inject(Object declaringInstance, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WeldParameter<T, X> getAnnotated() {
        return attributes.getAnnotated();
    }

    @Override
    public T getValueToInject(BeanManagerImpl manager, CreationalContext<?> creationalContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ParameterInjectionPointAttributes<T, X> delegate() {
        return attributes;
    }
}
