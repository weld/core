/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.weld.environment.tomcat7;

import org.jboss.weld.environment.AbstractContainer;
import org.jboss.weld.environment.Container;
import org.jboss.weld.environment.ContainerContext;

/**
 * Tomcat7 container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Tomcat7Container extends AbstractContainer {
    public static Container INSTANCE = new Tomcat7Container();

    protected String classToCheck() {
        return "org.apache.tomcat.InstanceManager";
    }

    public void initialize(ContainerContext context) {
        try {
            WeldForwardingInstanceManager.replacInstanceManager(context.getEvent(), context.getManager());
            log.info("Tomcat 7 detected, CDI injection will be available in Servlets and Filters. Injection into Listeners is not supported");
        } catch (Exception e) {
            log.error("Unable to replace Tomcat 7 AnnotationProcessor. CDI injection will not be available in Servlets, Filters, or Listeners", e);
        }
    }
}
