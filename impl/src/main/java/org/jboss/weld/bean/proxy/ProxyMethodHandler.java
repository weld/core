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

import javassist.util.proxy.MethodHandler;
import org.jboss.weld.Container;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.PassivationCapable;
import java.io.Serializable;
import java.lang.reflect.Method;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_INSTANCE_NOT_SET_ON_PROXY;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_HANDLER_SERIALIZED_FOR_NON_SERIALIZABLE_BEAN;

/**
 * A general purpose MethodHandler for all proxies which routes calls to the
 * {@link BeanInstance} associated with this proxy or handler.
 *
 * @author David Allen
 */
public class ProxyMethodHandler implements MethodHandler, Serializable {

    private static final long serialVersionUID = 5293834510764991583L;

    // The log provider
    protected static final LocLogger log = loggerFactory().getLogger(BEAN);

    // The bean instance to forward calls to
    private BeanInstance beanInstance;

    private final String beanId;

    private transient Bean<?> bean;

    private final String contextId;

    public ProxyMethodHandler(String contextId, BeanInstance beanInstance, Bean<?> bean) {
        this.beanInstance = beanInstance;
        this.bean = bean;
        this.contextId = contextId;
        if (bean instanceof PassivationCapable) {
            this.beanId = ((PassivationCapable) bean).getId();
        } else {
            this.beanId = null;
        }
    }

    /* (non-Javadoc)
    * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
    */
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        boolean traceEnabled = log.isTraceEnabled();
        if (thisMethod == null) {
            if (traceEnabled) {
                log.trace("MethodHandler processing returning bean instance for " + self.getClass());
            }
            if (beanInstance == null) {
                throw new WeldException(BEAN_INSTANCE_NOT_SET_ON_PROXY);
            }
            return beanInstance.getInstance();
        }
        if (traceEnabled) {
            log.trace("MethodHandler processing call to " + thisMethod + " for " + self.getClass());
        }
        if (thisMethod.getDeclaringClass().equals(TargetInstanceProxy.class)) {
            if (beanInstance == null) {
                throw new WeldException(BEAN_INSTANCE_NOT_SET_ON_PROXY);
            }
            if (thisMethod.getName().equals("getTargetInstance")) {
                return beanInstance.getInstance();
            } else if (thisMethod.getName().equals("getTargetClass")) {
                return beanInstance.getInstanceType();
            } else {
                return null;
            }
        } else if (thisMethod.getName().equals("_initMH")) {
            if (traceEnabled) {
                log.trace("Setting new MethodHandler with bean instance for " + args[0] + " on " + self.getClass());
            }
            return new ProxyMethodHandler(contextId, new TargetBeanInstance(args[0]), getBean());
        } else {
            if (beanInstance == null) {
                throw new WeldException(BEAN_INSTANCE_NOT_SET_ON_PROXY);
            }
            Object instance = beanInstance.getInstance();
            Object result = beanInstance.invoke(instance, thisMethod, args);
            // if the method returns this return the proxy instead
            // to prevent the bean instance escaping
            if (result != null && result == instance) {
                return self;
            }
            return result;
        }
    }

    private Bean<?> getBean() {
        if (bean == null) {
            if (beanId == null) {
                throw new WeldException(PROXY_HANDLER_SERIALIZED_FOR_NON_SERIALIZABLE_BEAN);
            }
            bean = Container.instance(contextId).services().get(ContextualStore.class).<Bean<Object>, Object>getContextual(beanId);
        }
        return bean;
    }


}
