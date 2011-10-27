/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.interceptors.extension;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.tests.interceptors.extension.FooInterceptorBinding.FooInterceptorBindingLiteral;

/**
 * Extension-provided implementation of {@link Interceptor}. Delegates to FooInterceptor (which in real world could represent a
 * legacy-style interceptor - e.g. Seam 2 interceptor)
 * 
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 */
public class CustomInterceptor extends AbstractInterceptor<FooInterceptor> implements PassivationCapable {

    private static boolean invoked = false;

    public Set<Annotation> getInterceptorBindings() {
        return Collections.<Annotation> singleton(FooInterceptorBindingLiteral.INSTANCE);
    }

    public boolean intercepts(InterceptionType type) {
        return InterceptionType.AROUND_INVOKE.equals(type);
    }

    public Object intercept(InterceptionType type, FooInterceptor instance, InvocationContext ctx) {
        invoked = true;
        try {
            return instance.intercept(ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getBeanClass() {
        return FooInterceptor.class;
    }

    public static boolean isInvoked() {
        return invoked;
    }

    public static void reset() {
        invoked = false;
    }

    public String getId() {
        return toString();
    }
}
