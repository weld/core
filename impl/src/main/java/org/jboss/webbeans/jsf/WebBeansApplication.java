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
package org.jboss.webbeans.jsf;

import java.util.ArrayList;
import java.util.Collection;

import javax.el.ELContextListener;
import javax.el.ExpressionFactory;
import javax.faces.application.Application;

import org.jboss.webbeans.el.WebBeansELContextListener;
import org.jboss.webbeans.el.WebBeansExpressionFactory;

/**
 * @author pmuir
 *
 */
public class WebBeansApplication extends ForwardingApplication
{
   
   private static final ELContextListener[] EMPTY_LISTENERS = {};
   
   private final Application application;
   private final Collection<ELContextListener> elContextListeners;
   
   public WebBeansApplication(Application application)
   {
      this.application = application;
      this.elContextListeners = new ArrayList<ELContextListener>();
      this.elContextListeners.add(new WebBeansELContextListener());
   }

   @Override
   protected Application delegate()
   {
      return application;
   }
   
   @Override
   public ExpressionFactory getExpressionFactory()
   {
      return new WebBeansExpressionFactory(delegate().getExpressionFactory());
   }
   
   @Override
   public ELContextListener[] getELContextListeners()
   {
      return elContextListeners.toArray(EMPTY_LISTENERS);
   }
   
   @Override
   public void addELContextListener(ELContextListener listener)
   {
      elContextListeners.add(listener);
   }
   
   @Override
   public void removeELContextListener(ELContextListener listener)
   {
      elContextListeners.remove(listener);
   }

}
