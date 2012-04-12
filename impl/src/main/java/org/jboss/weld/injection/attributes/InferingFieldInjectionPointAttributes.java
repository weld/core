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
import java.lang.reflect.Member;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that infers the attributes by reading {@link EnhancedAnnotatedField}.
 *
 * @author Jozef Hartinger
 *
 */
public class InferingFieldInjectionPointAttributes<T, X> extends AbstractInferingInjectionPointAttributes<T, Field> implements FieldInjectionPointAttributes<T, X> {

    private static final long serialVersionUID = -3099189770772787108L;

    public static <T, X> InferingFieldInjectionPointAttributes<T, X> of(EnhancedAnnotatedField<T, X> field, Bean<?> bean) {
        return new InferingFieldInjectionPointAttributes<T, X>(field, bean);
    }

    private final AnnotatedField<X> field;

    protected InferingFieldInjectionPointAttributes(EnhancedAnnotatedField<T, X> field, Bean<?> bean) {
        super(bean, SharedObjectFacade.wrap(field.getQualifiers()));
        this.field = field.slim();
    }

    @Override
    public Member getMember() {
        return getAnnotated().getJavaMember();
    }

    @Override
    public AnnotatedField<X> getAnnotated() {
        return field;
    }

    @Override
    public int hashCode() {
        return getAnnotated().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InferingFieldInjectionPointAttributes<?, ?>) {
            AnnotatedField<?> field = Reflections.<InferingFieldInjectionPointAttributes<?, ?>> cast(obj).getAnnotated();
            return AnnotatedTypes.compareAnnotatedField(getAnnotated(), field);
        }
        return false;
    }
}
