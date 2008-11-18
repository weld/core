package org.jboss.webbeans.test.mock;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession
{
   private Map<String, Object> attributes = new HashMap<String, Object>();
   private boolean isInvalid;
   private ServletContext servletContext;
   
   @SuppressWarnings("unchecked")
   public class IteratorEnumeration implements Enumeration
   {
      
      private Iterator iterator;
      
      public IteratorEnumeration(Iterator iterator)
      {
         this.iterator = iterator;
      }

      public boolean hasMoreElements()
      {
         return iterator.hasNext();
      }

      public Object nextElement()
      {
         return iterator.next();
      }

   }
   
   public MockHttpSession(ServletContext servletContext) 
   {
      this.servletContext = servletContext;
   }
   
   public boolean isInvalid()
   {
      return isInvalid;
   }

   public long getCreationTime()
   {
      return 0;
   }

   public String getId()
   {
      return null;
   }

   public long getLastAccessedTime()
   {
      return 0;
   }
   
   private int maxInactiveInterval;

   public void setMaxInactiveInterval(int max)
   {
      maxInactiveInterval = max;
   }

   public int getMaxInactiveInterval()
   {
      return maxInactiveInterval;
   }
   
   public HttpSessionContext getSessionContext()
   {
      throw new UnsupportedOperationException();
   }

   public Object getAttribute(String att)
   {
      return attributes.get(att);
   }

   public Object getValue(String att)
   {
      return getAttribute(att);
   }

   public Enumeration getAttributeNames()
   {
      return new IteratorEnumeration( attributes.keySet().iterator() );
   }

   public String[] getValueNames()
   {
      return attributes.keySet().toArray( new String[0] );
   }

   public void setAttribute(String att, Object value)
   {
      if (value==null)
      {
         attributes.remove(att);
      }
      else
      {
         attributes.put(att, value);
      }
   }

   public void putValue(String att, Object value)
   {
      setAttribute(att, value);
   }

   public void removeAttribute(String att)
   {
      attributes.remove(att);
   }

   public void removeValue(String att)
   {
      removeAttribute(att);
   }

   public void invalidate()
   {
      attributes.clear();
      isInvalid = true;
   }

   public boolean isNew()
   {
      return false;
   }

   public Map<String, Object> getAttributes()
   {
      return attributes;
   }

   public ServletContext getServletContext()
   {
      return servletContext;
   }

}
