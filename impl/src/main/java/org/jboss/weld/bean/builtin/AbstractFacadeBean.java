/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.cal10n.LocLogger;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED;

public abstract class AbstractFacadeBean<T> extends AbstractBuiltInBean<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    protected AbstractFacadeBean(String idSuffix, BeanManagerImpl manager) {
        super(idSuffix, manager);
    }

    public T create(CreationalContext<T> creationalContext) {
        InjectionPoint injectionPoint = Container.instance(getBeanManager().getContextId()).services().get(CurrentInjectionPoint.class).peek();
        if (injectionPoint != null) {
            return newInstance(injectionPoint, creationalContext);
        } else {
            log.warn(DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED, toString());
            return null;
        }
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        creationalContext.release();
    }

    protected abstract T newInstance(InjectionPoint injectionPoint, CreationalContext<T> creationalContext);

}
