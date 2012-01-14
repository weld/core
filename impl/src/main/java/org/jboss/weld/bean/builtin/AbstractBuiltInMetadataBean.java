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
package org.jboss.weld.bean.builtin;

import javax.decorator.Decorator;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Common superclass for {@link Bean}, {@link Interceptor} and {@link Decorator} builtin beans.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractBuiltInMetadataBean<T> extends AbstractFacadeBean<T> {

    public AbstractBuiltInMetadataBean(String idSuffix, Class<T> type, BeanManagerImpl beanManager) {
        super(idSuffix, beanManager, type);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // noop
    }

    protected WeldCreationalContext<?> getParentCreationalContext(CreationalContext<?> ctx) {
        if (ctx instanceof WeldCreationalContext<?>) {
            WeldCreationalContext<?> parentContext = ((WeldCreationalContext<?>) ctx).getParentCreationalContext();
            if (parentContext != null) {
                return parentContext;
            }
        }
        throw new IllegalArgumentException("Unable to determine parent creational context of " + ctx);
    }

    @Override
    public String toString() {
        return "Implicit Bean [" + getType().getName() + "] with qualifiers [@Default]";
    }
}
