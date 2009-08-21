/*
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
package org.jboss.webbeans.jsp;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import org.jboss.webbeans.el.WebBeansELResolverImpl;
import org.jboss.webbeans.servlet.ServletHelper;

/**
 * @author pmuir
 * 
 */
public class JspInitialization
{

   public void init(ServletContext context)
   {
      // JSP 2.1 specific check
      if (JspFactory.getDefaultFactory() == null || JspFactory.getDefaultFactory().getJspApplicationContext(context) == null)
      {
         return;
      }

      // get JspApplicationContext.
      JspApplicationContext jspAppContext = JspFactory.getDefaultFactory().getJspApplicationContext(context);

      // register compositeELResolver with JSP
      jspAppContext.addELResolver(new WebBeansELResolverImpl(ServletHelper.getModuleBeanManager(context)));

      // DOesn't really achieve much :-(
      //jspAppContext.addELContextListener(new WebBeansELContextListener());
   }

}
