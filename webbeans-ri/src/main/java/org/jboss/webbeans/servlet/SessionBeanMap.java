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

package org.jboss.webbeans.servlet;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.jboss.webbeans.contexts.AbstractBeanMap;
import org.jboss.webbeans.contexts.ApplicationContext;

/**
 * A BeanMap that uses a HTTP session as backing map
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.contexts.ApplicationContext
 */
public class SessionBeanMap extends AbstractBeanMap
{
   // The HTTP session context to use as backing map
   private HttpSession session;

   /**
    * Constructor
    * 
    * @param session The HTTP session
    */
   public SessionBeanMap(HttpSession session)
   {
      super();
      this.session = session;
   }

   /**
    * @see org.jboss.webbeans.contexts.AbstractBeanMap#getKeyPrefix()
    */
   @Override
   protected String getKeyPrefix()
   {
      return ApplicationContext.class.getName();
   }

   /**
    * @see org.jboss.webbeans.contexts.AbstractBeanMap#getAttribute()
    */
   @Override
   protected Object getAttribute(String key)
   {
      return session.getAttribute(key);
   }

   /**
    * @see org.jboss.webbeans.contexts.AbstractBeanMap#getAttributeNames()
    */
   @SuppressWarnings("unchecked")
   @Override
   protected Enumeration<String> getAttributeNames()
   {
      return session.getAttributeNames();
   }

   /**
    * @see org.jboss.webbeans.contexts.AbstractBeanMap#removeAttributes()
    */
   @Override
   protected void removeAttribute(String key)
   {
      session.removeAttribute(key);
   }

   /**
    * @see org.jboss.webbeans.contexts.AbstractBeanMap#setAttribute()
    */
   @Override
   protected void setAttribute(String key, Object instance)
   {
      session.setAttribute(key, instance);
   }

}
