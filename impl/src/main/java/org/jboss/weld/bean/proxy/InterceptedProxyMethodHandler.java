/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * @author Martin Kouba
 */
public class InterceptedProxyMethodHandler extends CombinedInterceptorAndDecoratorStackMethodHandler {

    private static final long serialVersionUID = -4749313040369863855L;

    private final Object instance;

    /**
     *
     * @param instance
     */
    public InterceptedProxyMethodHandler(Object instance) {
        this.instance = instance;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        if (BeanLogger.LOG.isTraceEnabled()) {
            BeanLogger.LOG.invokingMethodDirectly(thisMethod.toGenericString(), instance);
        }
        Object result = null;
        try {
            Reflections.ensureAccessible(thisMethod, instance);
            result = thisMethod.invoke(instance, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

    @Override
    public Object invoke(Stack stack, Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return super.invoke(stack, instance, thisMethod, proceed, args);
    }

}
