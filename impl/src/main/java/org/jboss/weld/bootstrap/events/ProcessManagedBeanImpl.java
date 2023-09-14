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
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.invoke.InvokerBuilder;

import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.invokable.InvokerBuilderImpl;
import org.jboss.weld.manager.BeanManagerImpl;

public class ProcessManagedBeanImpl<X> extends AbstractProcessClassBean<X, ManagedBean<X>> implements ProcessManagedBean<X> {

    protected static <X> void fire(BeanManagerImpl beanManager, ManagedBean<X> bean) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessManagedBeanImpl<X>(beanManager, bean) {
            }.fire();
        }
    }

    public ProcessManagedBeanImpl(BeanManagerImpl beanManager, ManagedBean<X> bean) {
        super(beanManager, ProcessManagedBean.class, new Type[] { bean.getAnnotated().getBaseType() }, bean);
    }

    public AnnotatedType<X> getAnnotatedBeanClass() {
        checkWithinObserverNotification();
        return getBean().getAnnotated();
    }

    @Override
    public Collection<AnnotatedMethod<? super X>> getInvokableMethods() {
        return getBean().getInvokableMethods();
    }

    @Override
    public InvokerBuilder<Invoker<X, ?>> createInvoker(AnnotatedMethod<? super X> annotatedMethod) {
        checkWithinObserverNotification();
        if (!getBean().getInvokableMethods().contains(annotatedMethod)) {
            // TODO better exception
            throw new IllegalArgumentException("Not an invokable method: " + annotatedMethod);
        }
        return new InvokerBuilderImpl<>(getBean().getType(), annotatedMethod.getJavaMember(), getBeanManager());
    }

}
