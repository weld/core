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
package org.jboss.weld.environment.tomcat;

import javax.el.ExpressionFactory;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import org.apache.jasper.runtime.JspApplicationContextImpl;

/**
 * The Weld JSP initialization listener
 * 
 * 
 * @author Pete Muir
 *
 */
public class JspInitialization
{
   
   private static final String EXPRESSION_FACTORY_NAME = "org.jboss.weld.el.ExpressionFactory";
   
   private static class WeldJspApplicationContextImpl extends ForwardingJspApplicationContextImpl
   {
      private final JspApplicationContextImpl delegate;
      private final ExpressionFactory expressionFactory;
      
      public WeldJspApplicationContextImpl(JspApplicationContextImpl delegate, ExpressionFactory expressionFactory)
      {
         this.delegate = delegate;
         this.expressionFactory = expressionFactory;
      }

      @Override
      protected JspApplicationContextImpl delegate()
      {
         return delegate;
      }
      
      @Override
      public ExpressionFactory getExpressionFactory()
      {
         return expressionFactory;
      }
      
   }
   
   public void initialize(ServletContext context)
   {
      // get JspApplicationContext.
      JspApplicationContext jspAppContext = JspFactory.getDefaultFactory().getJspApplicationContext(context);
      
      if (context.getAttribute(EXPRESSION_FACTORY_NAME) != null)
      {
         ExpressionFactory expressionFactory = (ExpressionFactory) context.getAttribute(EXPRESSION_FACTORY_NAME);
         // Hack into JBoss Web/Catalina to replace the ExpressionFactory
         JspApplicationContextImpl wrappedJspApplicationContextImpl = new WeldJspApplicationContextImpl(JspApplicationContextImpl.getInstance(context), expressionFactory);
         context.setAttribute(JspApplicationContextImpl.class.getName(), wrappedJspApplicationContextImpl);
      }
      // otherwise something went wrong starting WB, so don't register with JSP
   }
}
