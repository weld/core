/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.security;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.ReflectionLogger;

public abstract class GetDeclaredMethodAction extends AbstractReflectionAction {

    public static PrivilegedExceptionAction<Method> of(Class<?> javaClass, String methodName, Class<?>... parameterTypes) {
        return new ExceptionAction(javaClass, methodName, parameterTypes);
    }

    /**
     * Returns {@link PrivilegedAction} instead of {@link PrivilegedExceptionAction}. If {@link NoSuchMethodException} is thrown
     * it is wrapped within {@link WeldException} using
     * {@link ReflectionLogger#noSuchMethodWrapper(NoSuchMethodException, String)}.
     */
    public static PrivilegedAction<Method> wrapException(Class<?> javaClass, String methodName, Class<?>... parameterTypes) {
        return new WrappingAction(javaClass, methodName, parameterTypes);
    }

    protected final String methodName;
    protected final Class<?>[] parameterTypes;

    public GetDeclaredMethodAction(Class<?> javaClass, String methodName, Class<?>... parameterTypes) {
        super(javaClass);
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public Method run() throws NoSuchMethodException {
        return javaClass.getDeclaredMethod(methodName, parameterTypes);
    }

    private static class ExceptionAction extends GetDeclaredMethodAction implements PrivilegedExceptionAction<Method> {
        public ExceptionAction(Class<?> javaClass, String methodName, Class<?>[] parameterTypes) {
            super(javaClass, methodName, parameterTypes);
        }
    }

    private static class WrappingAction extends GetDeclaredMethodAction implements PrivilegedAction<Method> {

        public WrappingAction(Class<?> javaClass, String methodName, Class<?>[] parameterTypes) {
            super(javaClass, methodName, parameterTypes);
        }

        @Override
        public Method run() {
            try {
                return super.run();
            } catch (NoSuchMethodException e) {
                throw ReflectionLogger.LOG.noSuchMethodWrapper(e, e.getMessage());
            }
        }
    }
}
