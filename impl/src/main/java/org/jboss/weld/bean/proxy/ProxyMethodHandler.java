/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.Container;
import org.jboss.weld.bean.CommonBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.proxy.WeldClientProxy.Metadata;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A general purpose MethodHandler for all proxies which routes calls to the
 * {@link BeanInstance} associated with this proxy or handler.
 *
 * @author David Allen
 */
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "bean field is loaded lazily")
public class ProxyMethodHandler implements MethodHandler, Serializable, Metadata {

    private static final long serialVersionUID = 5293834510764991583L;

    // The bean instance to forward calls to
    private final BeanInstance beanInstance;

    private final BeanIdentifier beanId;

    private transient Bean<?> bean;

    private final String contextId;

    public ProxyMethodHandler(String contextId, BeanInstance beanInstance, Bean<?> bean) {
        this.beanInstance = beanInstance;
        this.bean = bean;
        this.contextId = contextId;
        if (bean instanceof CommonBean<?>) {
            this.beanId = ((CommonBean<?>) bean).getIdentifier();
        } else if (bean instanceof PassivationCapable) {
            this.beanId = new StringBeanIdentifier(((PassivationCapable) bean).getId());
        } else {
            this.beanId = null;
        }
    }

    @Override
    public Object getContextualInstance() {
        return this.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method,
     * java.lang.Object[])
     */
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        if (thisMethod == null) {
            BeanLogger.LOG.methodHandlerProcessingReturningBeanInstance(self.getClass());
            if (beanInstance == null) {
                throw BeanLogger.LOG.beanInstanceNotSetOnProxy(getBean());
            }
            return beanInstance.getInstance();
        }
        BeanLogger.LOG.methodHandlerProcessingCall(thisMethod, self.getClass());
        if (thisMethod.getDeclaringClass().equals(TargetInstanceProxy.class)) {
            if (beanInstance == null) {
                throw BeanLogger.LOG.beanInstanceNotSetOnProxy(getBean());
            }
            if (thisMethod.getName().equals("weld_getTargetInstance")) {
                return beanInstance.getInstance();
            } else if (thisMethod.getName().equals("weld_getTargetClass")) {
                return beanInstance.getInstanceType();
            } else {
                return null;
            }
        } else if (thisMethod.getName().equals("_initMH")) {
            BeanLogger.LOG.settingNewMethodHandler(args[0], self.getClass());
            return new ProxyMethodHandler(contextId, new TargetBeanInstance(args[0]), getBean());
        } else {
            if (beanInstance == null) {
                throw BeanLogger.LOG.beanInstanceNotSetOnProxy(getBean());
            }
            Object instance = beanInstance.getInstance();
            Object result = beanInstance.invoke(instance, thisMethod, args);
            // if the method returns this and the return type matches the proxy type, return the proxy instead
            // to prevent the bean instance escaping
            if (result != null && result == instance && (thisMethod.getReturnType().isAssignableFrom(self.getClass()))) {
                return self;
            }
            return result;
        }
    }

    public Bean<?> getBean() {
        if (bean == null) {
            if (beanId == null) {
                throw BeanLogger.LOG.proxyHandlerSerializedForNonSerializableBean();
            }
            bean = Container.instance(contextId).services().get(ContextualStore.class)
                    .<Bean<Object>, Object> getContextual(beanId);
        }
        return bean;
    }

    /**
     * Returns the underlying instance.
     *
     * @return the underlying instance
     */
    public Object getInstance() {
        return beanInstance.getInstance();
    }
}
