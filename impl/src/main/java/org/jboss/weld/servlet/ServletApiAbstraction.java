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
package org.jboss.weld.servlet;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ApiAbstraction;
import org.jboss.weld.util.reflection.Reflections;

public class ServletApiAbstraction extends ApiAbstraction implements Service {

    public static final String SERVLET_CONTEXT_CLASS_NAME = "javax.servlet.ServletContext";
    private static final String ASYNC_LISTENER_CONTEXT_CLASS_NAME = "javax.servlet.AsyncListener";

    private final boolean asyncSupported;

    public ServletApiAbstraction(ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.asyncSupported = Reflections.isClassLoadable(ASYNC_LISTENER_CONTEXT_CLASS_NAME, resourceLoader);
    }

    /**
     * Indicates, whether the version of Servlet API is available that has support for async processing (Servlet 3.0 and better)
     * @return
     */
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    @Override
    public void cleanup() {
    }
}
