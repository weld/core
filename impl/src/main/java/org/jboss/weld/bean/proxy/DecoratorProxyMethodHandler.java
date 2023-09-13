/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

import jakarta.enterprise.inject.spi.Decorator;
import jakarta.inject.Inject;

import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxyMethodHandler;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Method handler for decorated beans
 *
 * @author Pete Muir
 * @author Marius Bogoevici
 */
public class DecoratorProxyMethodHandler extends TargetInstanceProxyMethodHandler<Object> {
    private static final long serialVersionUID = 4577632640130385060L;

    private final SerializableContextualInstance<Decorator<Object>, Object> decoratorInstance;

    public DecoratorProxyMethodHandler(SerializableContextualInstance<Decorator<Object>, Object> decoratorInstance,
            Object delegateInstance) {
        super(delegateInstance, delegateInstance.getClass());
        this.decoratorInstance = decoratorInstance;
    }

    /**
     * @param self the proxy instance.
     * @param method the overridden method declared in the super class or
     *        interface.
     * @param proceed the forwarder method for invoking the overridden method. It
     *        is null if the overridden method is abstract or declared in the
     *        interface.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance. If a
     *        parameter type is a primitive type, the type of the array
     *        element is a wrapper class.
     * @return the resulting value of the method invocation.
     * @throws Throwable if the method invocation fails.
     */
    @Override
    protected Object doInvoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
        Decorator<Object> decorator = decoratorInstance.getContextual().get();
        if (decorator instanceof WeldDecorator<?>) {
            WeldDecorator<?> weldDecorator = (WeldDecorator<?>) decorator;
            return doInvoke(weldDecorator, decoratorInstance.getInstance(), method, args);
        } else {
            throw BeanLogger.LOG.unexpectedUnwrappedCustomDecorator(decorator);
        }
    }

    private Object doInvoke(WeldDecorator<?> weldDecorator, Object decoratorInstance, Method method, Object[] args)
            throws Throwable {
        if (!method.isAnnotationPresent(Inject.class)) {
            InvokableAnnotatedMethod<?> decoratorMethod = weldDecorator.getDecoratorMethod(method);
            if (decoratorMethod != null) {
                try {
                    return decoratorMethod.invokeOnInstance(decoratorInstance, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        }
        SecurityActions.ensureAccessible(method);
        return Reflections.invokeAndUnwrap(getTargetInstance(), method, args);
    }
}
