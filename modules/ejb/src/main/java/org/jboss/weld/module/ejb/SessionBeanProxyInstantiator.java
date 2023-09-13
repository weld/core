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
package org.jboss.weld.module.ejb;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.security.NewInstanceAction;

/**
 * Instantiator implementation that instantiates a proxy for a session bean.
 *
 * @author Jozef Hartinger
 */
class SessionBeanProxyInstantiator<T> implements Instantiator<T> {

    private final Class<T> proxyClass;
    private final SessionBeanImpl<T> bean;

    SessionBeanProxyInstantiator(EnhancedAnnotatedType<T> type, SessionBeanImpl<T> bean) {
        this.bean = bean;
        this.proxyClass = new EnterpriseProxyFactory<T>(type.getJavaClass(), bean).getProxyClass();
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        try {
            T instance = AccessController.doPrivileged(NewInstanceAction.of(proxyClass));
            if (!bean.getScope().equals(Dependent.class)) {
                ctx.push(instance);
            }
            ProxyFactory.setBeanInstance(bean.getBeanManager().getContextId(), instance, createEnterpriseTargetBeanInstance(),
                    bean);
            return instance;
        } catch (PrivilegedActionException e) {
            if (e.getCause() instanceof InstantiationException) {
                throw new WeldException(BeanLogger.LOG.proxyInstantiationFailed(this), e.getCause());
            } else if (e.getCause() instanceof IllegalAccessException) {
                throw new WeldException(BeanLogger.LOG.proxyInstantiationBeanAccessFailed(this), e.getCause());
            } else {
                throw new WeldException(e.getCause());
            }
        } catch (Exception e) {
            throw BeanLogger.LOG.sessionBeanProxyInstantiationFailed(bean, proxyClass, e);
        }
    }

    protected EnterpriseTargetBeanInstance createEnterpriseTargetBeanInstance() {
        if (bean.getEjbDescriptor().isStateless() || bean.getEjbDescriptor().isSingleton()) {
            return new InjectionPointPropagatingEnterpriseTargetBeanInstance(bean.getBeanClass(),
                    new EnterpriseBeanProxyMethodHandler<T>(bean), bean.getBeanManager());
        } else {
            return new EnterpriseTargetBeanInstance(bean.getBeanClass(), new EnterpriseBeanProxyMethodHandler<T>(bean));
        }
    }

    @Override
    public boolean hasInterceptorSupport() {
        return false;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return false;
    }

    public SessionBean<T> getBean() {
        return bean;
    }

    @Override
    public Constructor<T> getConstructor() {
        return null; // not relevant
    }
}
