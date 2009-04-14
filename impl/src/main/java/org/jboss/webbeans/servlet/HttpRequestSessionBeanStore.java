/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webbeans.servlet;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanstore.AbstractAttributeBackedBeanStore;
import org.jboss.webbeans.context.beanstore.BeanStoreNamingScheme;
import org.jboss.webbeans.context.beanstore.PrefixBeanStoreNamingScheme;

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
    * @see org.jboss.webbeans.context.beanstore.AbstractAttributeBackedBeanStore#getAttribute()
    */
   @Override
   protected Object getAttribute(String key)
   {
      HttpSession session = request.getSession(false);
      return session==null ? null : session.getAttribute( key );
   }

   /**
    * @see org.jboss.webbeans.context.beanstore.AbstractAttributeBackedBeanStore#getAttributeNames()
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
    * @see org.jboss.webbeans.context.beanstore.AbstractAttributeBackedBeanStore#removeAttributes()
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
    * @see org.jboss.webbeans.context.beanstore.AbstractAttributeBackedBeanStore#setAttribute()
    */
   @Override
   protected void setAttribute(String key, Object instance)
   {
      HttpSession session = request.getSession(true);
      session.setAttribute(key, instance);
   }

   @Override
   protected BeanStoreNamingScheme getNamingScheme()
   {
      return new PrefixBeanStoreNamingScheme(SessionContext.class.getName(), "#");
   }

}