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
package org.jboss.weld.tests.builtinBeans.metadata;

import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@javax.interceptor.Interceptor
@Frozen
public class YoghurtInterceptor {

    @Inject
    private Bean<YoghurtInterceptor> bean;
    @Inject
    private Interceptor<YoghurtInterceptor> interceptor;
    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        return this;
    }

    public Bean<YoghurtInterceptor> getBean() {
        return bean;
    }

    public Interceptor<YoghurtInterceptor> getInterceptor() {
        return interceptor;
    }

    public Bean<?> getInterceptedBean() {
        return interceptedBean;
    }
}
