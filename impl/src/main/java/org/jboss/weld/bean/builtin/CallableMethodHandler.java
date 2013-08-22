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
package org.jboss.weld.bean.builtin;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.reflection.Reflections;

public class CallableMethodHandler implements MethodHandler, Serializable {

    private static final long serialVersionUID = -1348302663981663427L;

    private final Callable<?> callable;

    public CallableMethodHandler(Callable<?> callable) {
        this.callable = callable;
    }

    public Object invoke(Object self, Method proxiedMethod, Method proceed, Object[] args) throws Throwable {
        Object instance = callable.call();
        if (instance == null) {
            throw BeanLogger.LOG.nullInstance(callable);
        }
        Object returnValue = Reflections.invokeAndUnwrap(instance, proxiedMethod, args);
        BeanLogger.LOG.callProxiedMethod(proxiedMethod, instance, args, returnValue == null ? null : returnValue);
        return returnValue;
    }

}
