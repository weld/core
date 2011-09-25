/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.environment.gwtdev;

import org.jboss.weld.environment.Container;
import org.jboss.weld.environment.jetty.Jetty6Container;

/**
 *
 */
public class GwtDevHostedModeContainer extends Jetty6Container {
    public static Container INSTANCE = new GwtDevHostedModeContainer();

    // The gwt-dev jar is never in the project classpath (only in the maven/eclipse/intellij plugin classpath)
    // except when GWT is being run in hosted mode.
    private static final String GWT_DEV_HOSTED_MODE_REQUIRED_CLASS_NAME = "com.google.gwt.dev.HostedMode";

    protected String classToCheck() {
        return GWT_DEV_HOSTED_MODE_REQUIRED_CLASS_NAME;
    }

}
