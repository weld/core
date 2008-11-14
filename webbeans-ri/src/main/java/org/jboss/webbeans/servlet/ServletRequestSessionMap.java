package org.jboss.webbeans.servlet;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ServletRequestSessionMap implements Map<String, Object>
{
   private HttpServletRequest request;

   public ServletRequestSessionMap(HttpServletRequest request)
   {
      this.request = request;
   }

   public void clear()
   {
      throw new UnsupportedOperationException(); 
   }

   public boolean containsKey(Object key)
   {
      HttpSession session = request.getSession(false);
      return session==null ? false : session.getAttribute( (String) key )!=null;
   }

   public boolean containsValue(Object value)
   {
      throw new UnsupportedOperationException();
   }

   public Set<java.util.Map.Entry<String, Object>> entrySet()
   {
      throw new UnsupportedOperationException();
   }

   public Object get(Object key)
   {
      HttpSession session = request.getSession(false);
      return session==null ? null : session.getAttribute( (String) key );
   }

   public boolean isEmpty()
   {
      throw new UnsupportedOperationException();
   }

   public Set<String> keySet()
   {
      HttpSession session = request.getSession(false);
      if (session==null)
      {
         return Collections.EMPTY_SET;
      }
      else
      {
         Set<String> keys = new HashSet<String>();
         Enumeration<String> names = session.getAttributeNames();
         while ( names.hasMoreElements() )
         {
            keys.add( names.nextElement() );
         }
         return keys;
      }
   }

   public Object put(String key, Object value)
   {
      HttpSession session = request.getSession(true);
      Object result = session.getAttribute(key);
      session.setAttribute(key, value);
      return result;
   }

   public void putAll(Map<? extends String, ? extends Object> t)
   {
      throw new UnsupportedOperationException();
   }

   public Object remove(Object key)
   {
      HttpSession session = request.getSession(false);
      if (session==null)
      {
         return null;
      }
      else
      {
         Object result = session.getAttribute( (String) key );
         session.removeAttribute( (String) key );
         return result;
      }
   }

   public int size()
   {
      throw new UnsupportedOperationException();
   }

   public Collection<Object> values()
   {
      throw new UnsupportedOperationException();
   }

}
