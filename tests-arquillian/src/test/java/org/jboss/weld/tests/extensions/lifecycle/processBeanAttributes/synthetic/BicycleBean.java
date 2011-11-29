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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.synthetic;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

public class BicycleBean extends ForwardingBeanAttributes<Bicycle> implements Bean<Bicycle> {

    private BeanAttributes<Bicycle> delegate;

    public BicycleBean(BeanAttributes<Bicycle> delegate) {
        this.delegate = delegate;
    }

    public Bicycle create(CreationalContext<Bicycle> creationalContext) {
        return new Bicycle();
    }

    public void destroy(Bicycle instance, CreationalContext<Bicycle> creationalContext) {
    }

    public Class<?> getBeanClass() {
        return Bicycle.class;
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    protected BeanAttributes<Bicycle> attributes() {
        return delegate;
    }

}
