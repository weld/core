/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.undertow;

import org.jboss.weld.environment.servlet.Container;
import org.jboss.weld.environment.servlet.ContainerContext;
import org.jboss.weld.environment.servlet.logging.UndertowLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

public class UndertowContainer implements Container {

    public static final UndertowContainer INSTANCE = new UndertowContainer();

    private static final String UDT_SERVLET_PREFIX = "io.undertow.servlet";

    @Override
    public boolean touch(ResourceLoader resourceLoader, ContainerContext context) throws Exception {
        return context.getServletContext().getClass().getName().startsWith(UDT_SERVLET_PREFIX);
    }

    @Override
    public void initialize(ContainerContext context) {
        Object value = context.getServletContext().getAttribute(WeldServletExtension.INSTALLED);
        if (WeldServletExtension.INSTALLED_FULL.equals(value)) {
            UndertowLogger.LOG.undertowDetected();
        } else if (WeldServletExtension.INSTALLED_SERVLET.equals(value)) {
            UndertowLogger.LOG.undertowDetectedServletOnly();
        }
    }

    @Override
    public void destroy(ContainerContext context) {
    }

}
