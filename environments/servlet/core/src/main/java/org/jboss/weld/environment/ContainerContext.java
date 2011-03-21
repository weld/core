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

package org.jboss.weld.environment;

import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.manager.api.WeldManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Wrap listner arguments.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ContainerContext
{
   private ServletContextEvent event;
   private ServletContext context;
   private WeldManager manager;

   public ContainerContext(ServletContextEvent event, WeldManager manager)
   {
      if (event == null)
         throw new IllegalArgumentException("Null servlet context event");

      this.event = event;
      this.context = event.getServletContext();
      if (manager == null)
         manager = (WeldManager) context.getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);

      this.manager = manager;
   }

   public ServletContextEvent getEvent()
   {
      return event;
   }

   public ServletContext getContext()
   {
      return context;
   }

   public WeldManager getManager()
   {
      return manager;
   }
}
