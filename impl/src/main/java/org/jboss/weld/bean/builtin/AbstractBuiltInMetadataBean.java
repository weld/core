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
