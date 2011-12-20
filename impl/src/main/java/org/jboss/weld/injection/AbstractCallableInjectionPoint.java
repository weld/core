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
package org.jboss.weld.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.introspector.WeldCallable;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Beans;

public abstract class AbstractCallableInjectionPoint<T, X, S extends Member> implements WeldInjectionPoint<T, S> {

    private final Bean<?> declaringBean;
    private final List<ParameterInjectionPoint<?, X>> parameters;

    protected AbstractCallableInjectionPoint(WeldCallable<T, X, S> callable, Bean<?> declaringBean, boolean observerOrDisposer, BeanManagerImpl manager) {
        this.declaringBean = declaringBean;
        if (observerOrDisposer) {
            this.parameters = Collections.unmodifiableList(Beans.getParameterInjectionPoints(callable, null, manager));
        } else {
            this.parameters = Collections.unmodifiableList(Beans.getParameterInjectionPoints(callable, declaringBean, manager));
        }
    }

    @Override
    public Type getType() {
        return getAnnotated().getBaseType();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return getAnnotated().getQualifiers();
    }

    @Override
    public Bean<?> getBean() {
        return declaringBean;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public <A extends Annotation> A getQualifier(Class<A> annotationType) {
        A qualifier = getAnnotated().getAnnotation(annotationType);
        if (getQualifiers().contains(qualifier)) {
            return qualifier;
        } else {
            return null;
        }
    }

    @Override
    public Member getMember() {
        return getAnnotated().getJavaMember();
    }

    @Override
    public abstract WeldCallable<T, X, S> getAnnotated();

    public List<ParameterInjectionPoint<?, X>> getParameterInjectionPoints() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractCallableInjectionPoint<?, ?, ?>) {
            AbstractCallableInjectionPoint<?, ?, ?> ip = (AbstractCallableInjectionPoint<?, ?, ?>) obj;
            if (AnnotatedTypes.compareAnnotatedCallable(getAnnotated(), ip.getAnnotated())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getAnnotated().hashCode();
    }
}
