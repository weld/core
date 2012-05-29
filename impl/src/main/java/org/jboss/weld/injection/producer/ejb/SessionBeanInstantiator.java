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
package org.jboss.weld.injection.producer.ejb;

import static org.jboss.weld.logging.messages.BeanMessage.EJB_NOT_FOUND;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.proxy.EnterpriseBeanProxyMethodHandler;
import org.jboss.weld.bean.proxy.EnterpriseProxyFactory;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.exceptions.CreationException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Instantiator implementation that instantiates a proxy for a session bean.
 *
 * @author Jozef Hartinger
 */
public class SessionBeanInstantiator<T> implements Instantiator<T> {

    private final Class<T> proxyClass;
    private final SessionBean<T> bean;

    public SessionBeanInstantiator(EnhancedAnnotatedType<T> type, SessionBean<T> bean) {
        this.bean = bean;
        this.proxyClass = new EnterpriseProxyFactory<T>(type.getJavaClass(), bean).getProxyClass();
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        try {
            T instance = SecureReflections.newInstance(proxyClass);
            ctx.push(instance);
            ProxyFactory.setBeanInstance(instance, new EnterpriseTargetBeanInstance(bean.getBeanClass(), new EnterpriseBeanProxyMethodHandler<T>(bean, ctx)), bean);
            return instance;
        } catch (InstantiationException e) {
            throw new WeldException(PROXY_INSTANTIATION_FAILED, e, this);
        } catch (IllegalAccessException e) {
            throw new WeldException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
        } catch (Exception e) {
            throw new CreationException(EJB_NOT_FOUND, e, proxyClass);
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
}
