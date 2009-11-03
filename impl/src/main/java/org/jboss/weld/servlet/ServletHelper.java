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
package org.jboss.weld.servlet;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;

import org.jboss.weld.BeanManagerImpl;

/**
 * @author pmuir
 *
 */
public class ServletHelper
{
   
   public static BeanManagerImpl getModuleBeanManager(ServletContext ctx)
   {
      if (ctx == null)
      {
         throw new IllegalArgumentException("ServletContext is null");
      }
      BeanManagerImpl beanManagerImpl = (BeanManagerImpl) ctx.getAttribute(BeanManager.class.getName());
      if (beanManagerImpl == null)
      {
         throw new IllegalArgumentException("Unable to find BeanManager. ServletContext: " + ctx);
      }
      else
      {
         return beanManagerImpl;
      }
   }

}
