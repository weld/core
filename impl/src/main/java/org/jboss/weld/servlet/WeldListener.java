/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.servlet;

import static org.jboss.weld.logging.Category.SERVLET;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ServletMessage.BEAN_DEPLOYMENT_ARCHIVE_MISSING;
import static org.jboss.weld.logging.messages.ServletMessage.BEAN_MANAGER_FOR_ARCHIVE_NOT_FOUND;
import static org.jboss.weld.logging.messages.ServletMessage.ILLEGAL_USE_OF_WELD_LISTENER;
import static org.jboss.weld.logging.messages.ServletMessage.NOT_STARTING;
import static org.jboss.weld.logging.messages.ServletMessage.ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.api.ServletServices;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.slf4j.cal10n.LocLogger;

/**
 * The Weld listener
 * 
 * Listens for context/session creation/destruction.
 * 
 * Delegates work to the ServletLifeCycle.
 * 
 * @author Nicklas Karlsson
 *
 */
public class WeldListener extends AbstractServletListener
{
   
   private static final LocLogger log = loggerFactory().getLogger(SERVLET);
   
   private ServletLifecycle lifecycle;
   
   private ServletLifecycle getLifecycle()
   {
      if (lifecycle == null)
      {
         this.lifecycle = new ServletLifecycle(Container.instance().deploymentServices().get(ContextLifecycle.class));
      }
      return lifecycle;
   }
   
   private static BeanManagerImpl getBeanManager(ServletContext ctx)
   {
      BeanDeploymentArchive war = Container.instance().deploymentServices().get(ServletServices.class).getBeanDeploymentArchive(ctx);
      if (war == null)
      {
         throw new ForbiddenStateException(BEAN_DEPLOYMENT_ARCHIVE_MISSING, ctx);
      }
      BeanManagerImpl beanManager = Container.instance().beanDeploymentArchives().get(war);
      if (beanManager == null)
      {
         throw new ForbiddenStateException(BEAN_MANAGER_FOR_ARCHIVE_NOT_FOUND, ctx, war);
      }
      return beanManager;
   }
   
   @Override
   public void contextInitialized(ServletContextEvent sce)
   {
      super.contextInitialized(sce);
      if (!Container.available())
      {
         log.warn(NOT_STARTING);
         return;
      }
      if (!Container.instance().deploymentServices().contains(ServletServices.class))
      {
         throw new ForbiddenStateException(ILLEGAL_USE_OF_WELD_LISTENER);
      }
      sce.getServletContext().setAttribute(BeanManager.class.getName(), getBeanManager(sce.getServletContext()));
   }
   
   @Override
   public void contextDestroyed(ServletContextEvent sce)
   {
      sce.getServletContext().removeAttribute(BeanManager.class.getName());
      super.contextDestroyed(sce);
   }

   /**
    * Called when the session is created
    * 
    * @param event The session event
    */
   @Override
   public void sessionCreated(HttpSessionEvent event) 
   {
      // JBoss AS will still start the deployment even if WB fails
      if (Container.available())
      {
         getLifecycle().beginSession(event.getSession());
      }
   }

   /**
    * Called when the session is destroyed
    * 
    * @param event The session event
    */
   @Override
   public void sessionDestroyed(HttpSessionEvent event) 
   {
      // JBoss AS will still start the deployment even if WB fails
      if (Container.available())
      {
         getLifecycle().endSession(event.getSession());
      }
   }

   /**
    * Called when the request is destroyed
    * 
    * @param event The request event
    */
   @Override
   public void requestDestroyed(ServletRequestEvent event)
   {
      // JBoss AS will still start the deployment even if WB fails
      if (Container.available())
      {
         if (event.getServletRequest() instanceof HttpServletRequest)
         {
            getLifecycle().endRequest((HttpServletRequest) event.getServletRequest());
         }
         else
         {
            throw new ForbiddenStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
         }
      }
   }

   /**
    * Called when the request is initialized
    * 
    * @param event The request event
    */
   @Override
   public void requestInitialized(ServletRequestEvent event)
   {
      // JBoss AS will still start the deployment even if WB fails
      if (Container.available())
      {
         if (event.getServletRequest() instanceof HttpServletRequest)
         {
            getLifecycle().beginRequest((HttpServletRequest) event.getServletRequest());
         }
         else
         {
            throw new ForbiddenStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
         }
      }
   }

}
