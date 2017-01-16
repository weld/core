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
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * Common superclass for {@link Bean}, {@link Interceptor} and {@link Decorator} builtin beans.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractBuiltInMetadataBean<T> extends AbstractBuiltInBean<T> {

    private final CurrentInjectionPoint cip;

    public AbstractBuiltInMetadataBean(BeanIdentifier identifier, Class<T> type, BeanManagerImpl beanManager) {
        super(identifier, beanManager, type);
        this.cip = beanManager.getServices().get(CurrentInjectionPoint.class);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        InjectionPoint ip = cip.peek();
        if (ip == null || EmptyInjectionPoint.INSTANCE.equals(ip)) {
            throw BeanLogger.LOG.dynamicLookupOfBuiltInNotAllowed(toString());
        }
        return newInstance(ip, creationalContext);
    }

    protected abstract T newInstance(InjectionPoint ip, CreationalContext<T> creationalContext);

    protected WeldCreationalContext<?> getParentCreationalContext(CreationalContext<?> ctx) {
        if (ctx instanceof WeldCreationalContext<?>) {
            WeldCreationalContext<?> parentContext = ((WeldCreationalContext<?>) ctx).getParentCreationalContext();
            if (parentContext != null) {
                return parentContext;
            }
        }
        throw BeanLogger.LOG.unableToDetermineParentCreationalContext(ctx);
    }

    @Override
    public String toString() {
        return "Implicit Bean [" + getType().getName() + "] with qualifiers [@Default]";
    }
}
