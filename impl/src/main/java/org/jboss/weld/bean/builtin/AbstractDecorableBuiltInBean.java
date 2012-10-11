/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED;

import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Decorators;
import org.slf4j.cal10n.LocLogger;

/**
 * Built-in bean that can be decorated by a {@link Decorator}
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractDecorableBuiltInBean<T> extends AbstractBuiltInBean<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private final CurrentInjectionPoint cip;

    protected AbstractDecorableBuiltInBean(String idSuffix, BeanManagerImpl beanManager, Class<T> type) {
        super(idSuffix, beanManager, type);
        this.cip = beanManager.getServices().get(CurrentInjectionPoint.class);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        InjectionPoint ip = cip.peek();
        // some of the built-in beans require injection point metadata while other can do without it
        if (ip == null && (isInjectionPointMetadataRequired() || !getDecorators(ip).isEmpty())) {
            injectionPointNotAvailable();
            return null;
        }

        List<Decorator<?>> decorators = getDecorators(ip);
        T instance = newInstance(ip, creationalContext);
        if (decorators.isEmpty()) {
            return instance;
        }
        return Decorators.getOuterDelegate(this, instance, creationalContext, getProxyClass(), cip.peek(), getBeanManager(), decorators);
    }

    protected abstract T newInstance(InjectionPoint ip, CreationalContext<T> creationalContext);

    protected abstract List<Decorator<?>> getDecorators(InjectionPoint ip);

    protected abstract Class<T> getProxyClass();

    protected boolean isInjectionPointMetadataRequired() {
        return false;
    }

    protected void injectionPointNotAvailable() {
        log.warn(DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED, toString());
    }

}
