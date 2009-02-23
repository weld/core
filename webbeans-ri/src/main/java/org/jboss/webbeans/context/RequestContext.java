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

package org.jboss.webbeans.context;

import javax.context.RequestScoped;

/**
 * The request context
 * 
 * @author Nicklas Karlsson
 */
public class RequestContext extends AbstractThreadLocalMapContext
{
	
   public static RequestContext INSTANCE;
   
   public static RequestContext create()
   {
      INSTANCE = new RequestContext();
      return INSTANCE;
   }

   /**
    * Constructor
    */
   protected RequestContext()
   {
      super(RequestScoped.class);
   }   

   @Override
   public String toString()
   {
      String active = isActive() ? "Active " : "Inactive ";
      String beanStoreInfo = getBeanStorage() == null ? "" : getBeanStorage().toString();
      return active + "request context " + beanStoreInfo; 
   }

}
