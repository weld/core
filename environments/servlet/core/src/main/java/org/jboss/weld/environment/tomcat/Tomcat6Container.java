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

package org.jboss.weld.environment.tomcat;

import org.jboss.weld.environment.AbstractContainer;
import org.jboss.weld.environment.Container;
import org.jboss.weld.environment.ContainerContext;

/**
 * Tomcat6.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Tomcat6Container extends AbstractContainer {
    public static Container INSTANCE = new Tomcat6Container();

    protected String classToCheck() {
        return "org.apache.catalina.core.ApplicationContextFacade";
    }

    public void initialize(ContainerContext context) {
        try {
            WeldForwardingAnnotationProcessor.replaceAnnotationProcessor(context.getServletContext(), context.getManager());
            log.info("Tomcat 6 detected, CDI injection will be available in Servlets and Filters. Injection into Listeners is not supported");
        } catch (Exception e) {
            log.error("Unable to replace Tomcat AnnotationProcessor. CDI injection will not be available in Servlets, Filters, or Listeners", e);
        }
    }

    @Override
    public void destroy(ContainerContext context) {
        WeldForwardingAnnotationProcessor.restoreAnnotationProcessor(context.getServletContext());
    }
}
