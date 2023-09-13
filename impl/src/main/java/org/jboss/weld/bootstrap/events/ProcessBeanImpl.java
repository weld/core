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

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.ProcessBean;

import org.jboss.weld.annotated.EmptyAnnotated;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class ProcessBeanImpl<X> extends AbstractDefinitionContainerEvent implements ProcessBean<X> {

    protected static <X> void fire(BeanManagerImpl beanManager, AbstractBean<X, ?> bean) {
        fire(beanManager, bean, bean.getAnnotated());
    }

    protected static <X> void fire(BeanManagerImpl beanManager, Bean<X> bean) {
        fire(beanManager, bean, EmptyAnnotated.INSTANCE);
    }

    private static <X> void fire(BeanManagerImpl beanManager, Bean<X> bean, Annotated annotated) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessBeanImpl<X>(beanManager, bean, annotated) {
            }.fire();
        }
    }

    private final Bean<X> bean;
    private final Annotated annotated;

    ProcessBeanImpl(BeanManagerImpl beanManager, Bean<X> bean, Annotated annotated) {
        super(beanManager, ProcessBean.class, new Type[] { bean.getBeanClass() });
        this.bean = bean;
        this.annotated = annotated;
    }

    public Annotated getAnnotated() {
        checkWithinObserverNotification();
        return annotated;
    }

    public Bean<X> getBean() {
        checkWithinObserverNotification();
        return bean;
    }

}
