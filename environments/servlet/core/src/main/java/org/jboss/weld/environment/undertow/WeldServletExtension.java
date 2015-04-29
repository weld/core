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

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletInfo;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.jboss.weld.environment.servlet.logging.UndertowLogger;

/**
 * Undertow extension that hooks into undertow's instance creation and delegates to Weld.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldServletExtension implements ServletExtension {

    public static final String INSTALLED = WeldServletExtension.class.getName() + ".installed";

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        // Servlet injection
        for (ServletInfo servlet : deploymentInfo.getServlets().values()) {
            InstanceFactory<? extends Servlet> factory = servlet.getInstanceFactory();
            UndertowLogger.LOG.debugv("Installing CDI support for {0}", servlet.getServletClass());
            servlet.setInstanceFactory(WeldInstanceFactory.of(factory, servletContext, servlet.getServletClass()));
        }
        servletContext.setAttribute(INSTALLED, true);
    }
}
