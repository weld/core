/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Method;

import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;

/**
 * The interface implemented by the invocation handler of a proxy instance. Implementations of this interface get the current
 * {@link InterceptionDecorationContext} stack passed as a parameter. That way, the number the ThreadLocal access is reduced.
 *
 * @author Jozef Hartinger
 */
public interface StackAwareMethodHandler extends MethodHandler {

    /**
     * Is called when a method is invoked on a proxy instance associated with this handler. This method must process that method
     * invocation.
     *
     * @param the current {@link InterceptionDecorationContext} stack
     * @param self the proxy instance.
     * @param thisMethod the overridden method declared in the super class or interface.
     * @param proceed the forwarder method for invoking the overridden method. It is null if the overridden method is abstract
     *        or declared in the interface.
     * @param args an array of objects containing the values of the arguments passed in the method invocation on the proxy
     *        instance. If a parameter type is a
     *        primitive type, the type of the array element is a wrapper class.
     * @return the resulting value of the method invocation.
     *
     * @throws Throwable if the method invocation fails.
     */
    Object invoke(Stack stack, Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable;
}
