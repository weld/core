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

package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;

/**
 * @author Ales Justin
 */
public class JettyDeployments {
    public static final Asset JETTY_ENV = new StringAsset("<Configure id=\"webAppCtx\" class=\"org.eclipse.jetty.webapp.WebAppContext\"><New class=\"org.eclipse.jetty.plus.naming.EnvEntry\"><Arg><Ref id=\"webAppCtx\"/></Arg><Arg>BeanManager</Arg><Arg><New class=\"javax.naming.Reference\"><Arg>javax.enterprise.inject.spi.BeanManager</Arg><Arg>org.jboss.weld.resources.ManagerObjectFactory</Arg><Arg/></New></Arg><Arg type=\"boolean\">true</Arg></New></Configure>");
}
