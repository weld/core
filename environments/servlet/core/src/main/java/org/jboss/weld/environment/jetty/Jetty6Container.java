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

package org.jboss.weld.environment.jetty;

import org.jboss.weld.environment.AbstractContainer;
import org.jboss.weld.environment.Container;
import org.jboss.weld.environment.ContainerContext;
import org.jboss.weld.environment.servlet.util.Reflections;
import org.jboss.weld.manager.api.WeldManager;

/**
 * Jetty6 container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Jetty6Container extends AbstractContainer
{
   public static Container INSTANCE = new Jetty6Container();

   private static final String JETTY_REQUIRED_CLASS_NAME = "org.mortbay.jetty.servlet.ServletHandler";
   public  static final String INJECTOR_ATTRIBUTE_NAME = "org.jboss.weld.environment.jetty.JettyWeldInjector";

   protected String classToCheck()
   {
      return JETTY_REQUIRED_CLASS_NAME;
   }

   public void initialize(ContainerContext context)
   {
      // Try pushing a Jetty Injector into the servlet context
      try
      {
         Class<?> clazz = Reflections.classForName(JettyWeldInjector.class.getName());
         Object injector = clazz.getConstructor(WeldManager.class).newInstance(context.getManager());
         context.getContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);
         log.info("Jetty detected, JSR-299 injection will be available in Servlets and Filters. Injection into Listeners is not supported.");
      }
      catch (Exception e)
      {
         log.error("Unable to create JettyWeldInjector. CDI injection will not be available in Servlets, Filters or Listeners", e);
      }
   }

   @Override
   public void destroy(ContainerContext context)
   {
      context.getContext().removeAttribute(INJECTOR_ATTRIBUTE_NAME);
   }
}
