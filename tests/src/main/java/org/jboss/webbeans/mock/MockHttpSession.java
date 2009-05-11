package org.jboss.webbeans.mock;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * A mock implementation of the HttpSession interface for tests.
 * 
 * @author Dan Allen
 */
public class MockHttpSession implements HttpSession
{
   private String id;
   
   private ServletContext servletContext;
   
   private Map<String, Object> attributes = new HashMap<String, Object>();

   private boolean invalid = false;
   
   private int maxInactiveInterval = 60;
   
   private int lastAccessedTime = -1;
   
   public MockHttpSession() {}
   
   public MockHttpSession(String id)
   {
      this.id = id;
   }
   
   public MockHttpSession(String id, ServletContext servletContext)
   {
      this(id);
      this.servletContext = servletContext;
   }

   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   public Enumeration<String> getAttributeNames()
   {
      final Iterator<String> nameIterator = attributes.keySet().iterator();
      return new Enumeration<String>()
      {

         public boolean hasMoreElements()
         {
            return nameIterator.hasNext();
         }

         public String nextElement()
         {
            return nameIterator.next();
         }
      };
   }

   public long getCreationTime()
   {
      return 0;
   }

   public String getId()
   {
      return id;
   }

   public long getLastAccessedTime()
   {
      return lastAccessedTime;
   }

   public int getMaxInactiveInterval()
   {
      return maxInactiveInterval;
   }

   public ServletContext getServletContext()
   {
      return servletContext;
   }

   @SuppressWarnings("deprecation")
   public HttpSessionContext getSessionContext()
   {
      throw new UnsupportedOperationException();
   }

   public Object getValue(String name)
   {
      return getAttribute(name);
   }

   public String[] getValueNames()
   {
      return attributes.keySet().toArray(new String[0]);
   }

   public void invalidate()
   {
      attributes.clear();
      invalid = true;
   }

   public boolean isNew()
   {
      return false;
   }

   public void putValue(String name, Object value)
   {
      setAttribute(name, value);
   }

   public void removeAttribute(String name)
   {
      attributes.remove(name);
   }

   public void removeValue(String name)
   {
      removeAttribute(name);
   }

   public void setAttribute(String name, Object value)
   {
      if (value == null)
      {
         removeAttribute(name);
      }
      else
      {
         attributes.put(name, value);
      }
   }

   public void setMaxInactiveInterval(int seconds)
   {
      maxInactiveInterval = seconds;
   }
   
   public boolean isInvalid()
   {
      return invalid;
   }
   
   public void access()
   {
      lastAccessedTime = (int) System.currentTimeMillis();
   }

}
