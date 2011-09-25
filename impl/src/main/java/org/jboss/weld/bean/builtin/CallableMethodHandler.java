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

import javassist.util.proxy.MethodHandler;
import org.jboss.weld.exceptions.NullInstanceException;
import org.jboss.weld.util.reflection.SecureReflections;
import org.slf4j.cal10n.LocLogger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CALL_PROXIED_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.NULL_INSTANCE;

public class CallableMethodHandler implements MethodHandler, Serializable {

    private static final long serialVersionUID = -1348302663981663427L;
    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private final Callable<?> callable;

    public CallableMethodHandler(Callable<?> callable) {
        this.callable = callable;
    }

    public Object invoke(Object self, Method proxiedMethod, Method proceed, Object[] args) throws Throwable {
        Object instance = callable.call();
        if (instance == null) {
            throw new NullInstanceException(NULL_INSTANCE, callable);
        }
        try {
            Object returnValue = SecureReflections.invoke(instance, proxiedMethod, args);
            log.trace(CALL_PROXIED_METHOD, proxiedMethod, instance, args, returnValue == null ? null : returnValue);
            return returnValue;
        } catch (InvocationTargetException e) {
            // Unwrap the ITE
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

}
