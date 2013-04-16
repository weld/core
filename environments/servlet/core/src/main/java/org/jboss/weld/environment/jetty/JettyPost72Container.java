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

import org.jboss.weld.environment.Container;
import org.jboss.weld.environment.ContainerContext;
import org.jboss.weld.environment.servlet.util.Reflections;
import org.jboss.weld.manager.api.WeldManager;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;

/**
 * Jetty 7.2+ container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JettyPost72Container extends AbstractJettyContainer {
    public static Container INSTANCE = new JettyPost72Container();

    private static final int MAJOR_VERSION = 7;
    private static final int MINOR_VERSION = 2;

    protected String classToCheck() {
        throw new IllegalAccessError("Should not be used!");
    }

    public boolean touch(ContainerContext context) throws Exception {
        ServletContext sc = context.getContext();
        String si = sc.getServerInfo();
        int p = si.indexOf("/");
        if (p < 0) {
            return false;
        }

        String version = si.substring(p + 1);
        String[] split = version.split("\\.");
        int major = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        return (major > MAJOR_VERSION || (major == MAJOR_VERSION & minor >= MINOR_VERSION));
    }

    public void initialize(ContainerContext context) {
        // Try pushing a Jetty Injector into the servlet context
        try {
            Class<?> clazz = Reflections.classForName(JettyWeldInjector.class.getName());
            Object injector = clazz.getConstructor(WeldManager.class).newInstance(context.getManager());
            context.getContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);

            Class<?> decoratorClass = Reflections.classForName("org.jboss.weld.environment.jetty.WeldDecorator");
            Method processMethod = decoratorClass.getMethod("process", ServletContext.class);
            processMethod.invoke(null, context.getContext());

            log.info("Jetty7 detected, JSR-299 injection will be available in Listeners, Servlets and Filters.");
        } catch (Exception e) {
            log.error("Unable to create JettyWeldInjector. CDI injection will not be available in Servlets, Filters or Listeners", e);
        }
    }
}
