/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.weld.util.reflection.Reflections;

/**
 * This method handler is used to invoke a private method which is not intercepted - Weld cannot use invokespecial in this case.
 *
 * @author Martin Kouba
 * @see InterceptedSubclassFactory
 */
class PrivateMethodHandler implements MethodHandler, Serializable {

    static final PrivateMethodHandler INSTANCE = new PrivateMethodHandler();
    private static final long serialVersionUID = -1785189321581448377L;

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        Object result = null;
        try {
            Reflections.ensureAccessible(thisMethod, self);
            result = thisMethod.invoke(self, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

}
