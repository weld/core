package org.jboss.webbeans.servlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

public class ServletRequestMap implements Map<String, Object>
{
   
   private ServletRequest request;

   public ServletRequestMap(ServletRequest request)
   {
      this.request = request;
   }

   public void clear()
   {
      throw new UnsupportedOperationException(); 
   }

   public boolean containsKey(Object key)
   {
      return request.getAttribute( (String) key )!=null;
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
      return request.getAttribute( (String) key );
   }

   public boolean isEmpty()
   {
      throw new UnsupportedOperationException();
   }

   public Set<String> keySet()
   {
      Set<String> keys = new HashSet<String>();
      Enumeration<String> names = request.getAttributeNames();
      while ( names.hasMoreElements() )
      {
         keys.add( names.nextElement() );
      }
      return keys;
   }

   public Object put(String key, Object value)
   {
      Object result = request.getAttribute(key);
      request.setAttribute(key, value);
      return result;
   }

   public void putAll(Map<? extends String, ? extends Object> t)
   {
      throw new UnsupportedOperationException();
   }

   public Object remove(Object key)
   {
      Object result = request.getAttribute( (String) key );
      request.removeAttribute( (String) key );
      return result;
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
