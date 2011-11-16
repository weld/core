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

import javax.decorator.Delegate;
import javax.enterprise.inject.Decorated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.inject.Inject;

@javax.decorator.Decorator
@SuppressWarnings("unused")
public class MilkProductDecorator implements MilkProduct {

    @Inject
    @Delegate
    private MilkProduct delegate;
    @Inject
    private Bean<MilkProductDecorator> bean;
    @Inject
    private Decorator<MilkProductDecorator> decorator;
    @Inject
    @Decorated
    private Bean<? extends MilkProduct> decoratedBean;

    public Bean<MilkProductDecorator> getBean() {
        return bean;
    }

    public Decorator<MilkProductDecorator> getDecorator() {
        return decorator;
    }

    public Bean<? extends MilkProduct> getDecoratedBean() {
        return decoratedBean;
    }

    public MilkProductDecorator getDecoratorInstance() {
        return this;
    }
}
