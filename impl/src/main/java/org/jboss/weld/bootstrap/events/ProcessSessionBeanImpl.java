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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.Collection;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.ProcessSessionBean;
import jakarta.enterprise.inject.spi.SessionBeanType;

import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.invoke.InvokerBuilder;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.invokable.InvokerBuilderImpl;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;

public class ProcessSessionBeanImpl<X> extends AbstractProcessClassBean<Object, SessionBean<Object>>
        implements ProcessSessionBean<X> {

    protected static <X> void fire(BeanManagerImpl beanManager, SessionBean<Object> bean) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessSessionBeanImpl<X>(beanManager, bean) {
            }.fire();
        }
    }

    private ProcessSessionBeanImpl(BeanManagerImpl beanManager, SessionBean<Object> bean) {
        super(beanManager, ProcessSessionBean.class, new Type[] { bean.getAnnotated().getBaseType() }, bean);
    }

    public String getEjbName() {
        checkWithinObserverNotification();
        return getBean().getEjbDescriptor().getEjbName();
    }

    public SessionBeanType getSessionBeanType() {
        checkWithinObserverNotification();
        if (getBean().getEjbDescriptor().isStateless()) {
            return SessionBeanType.STATELESS;
        } else if (getBean().getEjbDescriptor().isStateful()) {
            return SessionBeanType.STATEFUL;
        } else if (getBean().getEjbDescriptor().isSingleton()) {
            return SessionBeanType.SINGLETON;
        } else {
            throw BootstrapLogger.LOG.beanTypeNotEjb(getBean());
        }
    }

    public AnnotatedType<Object> getAnnotatedBeanClass() {
        checkWithinObserverNotification();
        return getBean().getAnnotated();
    }

    @Override
    public Collection<AnnotatedMethod<? super Object>> getInvokableMethods() {
        return getBean().getInvokableMethods();
    }

    @Override
    public InvokerBuilder<Invoker<Object, ?>> createInvoker(AnnotatedMethod<? super Object> annotatedMethod) {
        checkWithinObserverNotification();
        return new InvokerBuilderImpl<>(getBean().getEjbDescriptor().getBeanClass(), annotatedMethod.getJavaMember(), getBeanManager());
    }
}
