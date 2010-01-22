/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.SessionContext;
import org.jboss.weld.context.beanstore.AbstractAttributeBackedBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;

/**
 * Abstracts the servlet API specific session context
 * as a BeanStore. Actual sessions are created lazily.
 * 
 * @author Gavin King
 */
public class HttpRequestSessionBeanStore extends AbstractAttributeBackedBeanStore
{
   // The HTTP session context to use as backing map
   private final HttpServletRequest request;
   private static final NamingScheme NAMING_SCHEME = new NamingScheme(SessionContext.class.getName(), "#");

   /**
    * Constructor
    * 
    * @param session The HTTP session
    */
   public HttpRequestSessionBeanStore(HttpServletRequest request)
   {
      super();
      this.request = request;
   }

   /**
    * @see org.jboss.weld.context.beanstore.AbstractAttributeBackedBeanStore#getAttribute()
    */
   @Override
   protected Object getAttribute(String key)
   {
      HttpSession session = request.getSession(false);
      return session==null ? null : session.getAttribute( key );
   }

   /**
    * @see org.jboss.weld.context.beanstore.AbstractAttributeBackedBeanStore#getAttributeNames()
    */
   @SuppressWarnings("unchecked")
   @Override
   protected Enumeration<String> getAttributeNames()
   {
      HttpSession session = request.getSession(false);
      if (session != null)
      {
         return session.getAttributeNames();
      }
      else
      {
         return Collections.enumeration(Collections.EMPTY_LIST);
      }
   }

   /**
    * @see org.jboss.weld.context.beanstore.AbstractAttributeBackedBeanStore#removeAttributes()
    */
   @Override
   protected void removeAttribute(String key)
   {
      HttpSession session = request.getSession(false);
      if (session != null)
      {
         session.removeAttribute( key );
      }
   }

   /**
    * @see org.jboss.weld.context.beanstore.AbstractAttributeBackedBeanStore#setAttribute()
    */
   @Override
   protected void setAttribute(String key, Object instance)
   {
      HttpSession session = request.getSession(true);
      session.setAttribute(key, instance);
   }

   @Override
   protected NamingScheme getNamingScheme()
   {
      return NAMING_SCHEME;
   }

}