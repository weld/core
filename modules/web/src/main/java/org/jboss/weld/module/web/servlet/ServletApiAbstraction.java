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
package org.jboss.weld.module.web.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.module.web.logging.ServletLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ApiAbstraction;
import org.jboss.weld.util.reflection.Reflections;

public class ServletApiAbstraction extends ApiAbstraction implements Service {

    public static final String SERVLET_CONTEXT_CLASS_NAME = "javax.servlet.ServletContext";
    private static final String ASYNC_LISTENER_CONTEXT_CLASS_NAME = "javax.servlet.AsyncListener";
    private static final String SERVLET_REQUEST_CLASS_NAME = "javax.servlet.ServletRequest";
    private static final String IS_ASYNC_STARTED_METHOD_NAME = "isAsyncStarted";

    private final boolean asyncSupported;

    private final Method isAsyncStartedMethod;

    public ServletApiAbstraction(ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.asyncSupported = Reflections.isClassLoadable(ASYNC_LISTENER_CONTEXT_CLASS_NAME, resourceLoader);
        Method isAsyncStartedMethodLocal = null;

        Class<ServletRequest> servletRequestClass = Reflections.loadClass(SERVLET_REQUEST_CLASS_NAME, resourceLoader);
        if (servletRequestClass != null) {
            try {
                isAsyncStartedMethodLocal = servletRequestClass.getMethod(IS_ASYNC_STARTED_METHOD_NAME);
            } catch (NoSuchMethodException e) {
                ServletLogger.LOG.servlet2Environment();
            }
        }
        isAsyncStartedMethod = isAsyncStartedMethodLocal;
    }

    /**
     * Indicates, whether the version of Servlet API is available that has support for async processing (Servlet 3.0 and better)
     *
     * @return
     */
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    /**
     * Evaluates javax.servlet.ServletRequest#isAsyncStarted() method on given ServletRequest instance via reflection api.
     *
     * @param request
     * @return true if servletRequest started asynchronously, otherwise returns false.
     */
    public boolean isAsyncStarted(ServletRequest request) {

        if (isAsyncStartedMethod != null) {
            try {
                return (Boolean) isAsyncStartedMethod.invoke(request);
            } catch (IllegalAccessException e) {
                ServletLogger.LOG.error(e);
                return false;
            } catch (InvocationTargetException e) {
                ServletLogger.LOG.error(e);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void cleanup() {
    }
}
