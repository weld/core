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

import java.lang.reflect.Member;

import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that infers the attributes by reading {@link EnhancedAnnotatedParameter}.
 *
 * @author Jozef Hartinger
 *
 */
public class InferingParameterInjectionPointAttributes<T, X> extends AbstractInferingInjectionPointAttributes<T, Object> implements ParameterInjectionPointAttributes<T, X> {

    private static final long serialVersionUID = 1237037554422642608L;

    public static <T, X> InferingParameterInjectionPointAttributes<T, X> of(EnhancedAnnotatedParameter<T, X> parameter, Bean<?> bean) {
        return new InferingParameterInjectionPointAttributes<T, X>(parameter, bean);
    }

    private final AnnotatedParameter<X> parameter;

    protected InferingParameterInjectionPointAttributes(EnhancedAnnotatedParameter<T, X> parameter, Bean<?> bean) {
        super(bean, parameter.getQualifiers());
        this.parameter = parameter.slim();
    }

    @Override
    public Member getMember() {
        return getAnnotated().getDeclaringCallable().getJavaMember();
    }

    @Override
    public AnnotatedParameter<X> getAnnotated() {
        return parameter;
    }

    @Override
    public int hashCode() {
        return getAnnotated().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InferingParameterInjectionPointAttributes<?, ?>) {
            AnnotatedParameter<?> parameter = Reflections.<InferingParameterInjectionPointAttributes<?, ?>> cast(obj).getAnnotated();
            return AnnotatedTypes.compareAnnotatedParameters(getAnnotated(), parameter);
        }
        return false;
    }
}
