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

import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.SessionBeanType;
import java.lang.reflect.Type;

import static org.jboss.weld.logging.messages.BootstrapMessage.BEAN_TYPE_NOT_EJB;
import static org.jboss.weld.util.reflection.Reflections.cast;

public class ProcessSessionBeanImpl<X> extends AbstractProcessClassBean<Object, SessionBean<Object>> implements ProcessSessionBean<X> {

    protected static <X> void fire(BeanManagerImpl beanManager, SessionBean<Object> bean) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessSessionBeanImpl<X>(beanManager, bean) {
            }.fire();
        }
    }

    private ProcessSessionBeanImpl(BeanManagerImpl beanManager, SessionBean<Object> bean) {
        super(beanManager, ProcessSessionBean.class, new Type[]{bean.getAnnotated().getBaseType()}, bean);
    }

    public AnnotatedType<X> getAnnotatedSessionBeanClass() {
        return cast(getBean().getAnnotated());
    }

    public String getEjbName() {
        return getBean().getEjbDescriptor().getEjbName();
    }

    public SessionBeanType getSessionBeanType() {
        if (getBean().getEjbDescriptor().isStateless()) {
            return SessionBeanType.STATELESS;
        } else if (getBean().getEjbDescriptor().isStateful()) {
            return SessionBeanType.STATEFUL;
        } else if (getBean().getEjbDescriptor().isSingleton()) {
            return SessionBeanType.SINGLETON;
        } else {
            throw new IllegalStateException(BEAN_TYPE_NOT_EJB, getBean());
        }
    }

    public AnnotatedType<Object> getAnnotatedBeanClass() {
        return getBean().getAnnotated();
    }

}
