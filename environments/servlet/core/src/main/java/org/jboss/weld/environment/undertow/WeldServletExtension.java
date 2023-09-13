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

import java.util.EventListener;

import jakarta.servlet.ServletContext;

import org.jboss.weld.environment.servlet.logging.UndertowLogger;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

/**
 * Undertow extension that hooks into undertow's instance creation and delegates to Weld.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldServletExtension implements ServletExtension {

    public static final String INSTALLED = WeldServletExtension.class.getName() + ".installed";
    public static final String INSTALLED_SERVLET = "servlet-only";
    public static final String INSTALLED_FULL = "full";

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        // Servlet injection
        for (ServletInfo servlet : deploymentInfo.getServlets().values()) {
            UndertowLogger.LOG.installingCdiSupport(servlet.getServletClass());
            servlet.setInstanceFactory(
                    WeldInstanceFactory.of(servlet.getInstanceFactory(), servletContext, servlet.getServletClass()));
        }
        try {
            // Filter injection
            for (FilterInfo filter : deploymentInfo.getFilters().values()) {
                UndertowLogger.LOG.installingCdiSupport(filter.getFilterClass());
                filter.setInstanceFactory(
                        WeldInstanceFactory.of(filter.getInstanceFactory(), servletContext, filter.getFilterClass()));
            }
            // Listener injection
            for (ListenerInfo listener : deploymentInfo.getListeners()) {
                UndertowLogger.LOG.installingCdiSupport(listener.getListenerClass());
                InstanceFactory<? extends EventListener> instanceFactory = listener.getInstanceFactory();
                if (!(instanceFactory instanceof ImmediateInstanceFactory)) {
                    listener.setInstanceFactory(
                            WeldInstanceFactory.of(instanceFactory, servletContext, listener.getListenerClass()));
                }
            }
            servletContext.setAttribute(INSTALLED, INSTALLED_FULL);
        } catch (NoSuchMethodError e) {
            // Undertow 1.2 and older does not have setInstanceFactory() on listeners/filters
            servletContext.setAttribute(INSTALLED, INSTALLED_SERVLET);
            return;
        }
    }
}
