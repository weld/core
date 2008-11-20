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

package org.jboss.webbeans.contexts;

import javax.servlet.http.HttpSession;
import javax.webbeans.SessionScoped;

import org.apache.log4j.Logger;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.Bootstrap;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * The session context
 * 
 * @author Nicklas Karlsson
 */
public class SessionContext extends PrivateContext {

   private static LogProvider log = Logging.getLogProvider(SessionContext.class);
   
   public SessionContext(ManagerImpl manager)
   {
      super(SessionScoped.class);
      // Replaces the BeanMap implementation with a session-based one
      beans.set(new SessionBeanMap(manager, getScopeType().getName() + "#"));
      log.trace("Created session context");
   }
 
   /**
    * Sets the session in the session bean map
    * 
    * @param session The session to set
    */
   public void setSession(HttpSession session) {
      ((SessionBeanMap)getBeanMap()).setSession(session);
   }
   
   @Override
   public String toString()
   {
      return 
         "Session context:\n" + 
         "Active: " + getActive().toString() + 
         "Beans: " + getBeanMap().toString();
   }   
   

}
