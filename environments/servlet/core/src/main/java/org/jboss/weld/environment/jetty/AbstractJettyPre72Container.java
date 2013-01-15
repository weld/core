/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.jetty;

import org.jboss.weld.environment.ContainerContext;
import org.jboss.weld.environment.servlet.util.Reflections;
import org.jboss.weld.manager.api.WeldManager;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;

/**
 * Jetty 6 or 7 (pre 7.2) container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractJettyPre72Container extends AbstractJettyContainer {

    protected abstract Class<?> getWeldServletHandlerClass();

    public void initialize(ContainerContext context) {
        // Try pushing a Jetty Injector into the servlet context
        try {
            Class<?> clazz = Reflections.classForName(JettyWeldInjector.class.getName());
            Object injector = clazz.getConstructor(WeldManager.class).newInstance(context.getManager());
            context.getContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);
            log.info("Jetty detected, JSR-299 injection will be available in Servlets and Filters. Injection into Listeners is not supported.");

            Class<?> decoratorClass = getWeldServletHandlerClass();
            Method processMethod = decoratorClass.getMethod("process", ServletContext.class);
            processMethod.invoke(null, context.getContext());
        } catch (Exception e) {
            log.error("Unable to create JettyWeldInjector. CDI injection will not be available in Servlets, Filters or Listeners", e);
        }
    }
}
