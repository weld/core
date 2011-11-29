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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.synthetic.interceptor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

public class ExternalInterceptorBean extends ForwardingBeanAttributes<ExternalInterceptor> implements Interceptor<ExternalInterceptor> {

    private final ExternalInterceptor instance = new ExternalInterceptor();
    private final BeanAttributes<ExternalInterceptor> delegate;
    private final Annotation binding;

    public ExternalInterceptorBean(BeanAttributes<ExternalInterceptor> delegate, Annotation binding) {
        this.delegate = delegate;
        this.binding = binding;
    }

    public Class<?> getBeanClass() {
        return ExternalInterceptor.class;
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    public ExternalInterceptor create(CreationalContext<ExternalInterceptor> creationalContext) {
        return instance;
    }

    public void destroy(ExternalInterceptor instance, CreationalContext<ExternalInterceptor> creationalContext) {
    }

    public Set<Annotation> getInterceptorBindings() {
        return Collections.<Annotation> singleton(binding);
    }

    public boolean intercepts(InterceptionType type) {
        return true;
    }

    public Object intercept(InterceptionType type, ExternalInterceptor instance, InvocationContext ctx) throws Exception {
        return instance.intercept(ctx);
    }

    @Override
    protected BeanAttributes<ExternalInterceptor> attributes() {
        return delegate;
    }
}
