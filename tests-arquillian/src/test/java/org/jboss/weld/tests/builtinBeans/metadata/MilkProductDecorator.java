/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private Bean<MilkProduct> decoratedBean;

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
