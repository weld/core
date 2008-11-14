package org.jboss.webbeans.servlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

public class ServletApplicationMap implements Map<String, Object>
{
   private ServletContext servletContext;

   public ServletApplicationMap(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   public void clear()
   {
      throw new UnsupportedOperationException(); 
   }

   public boolean containsKey(Object key)
   {
      return servletContext.getAttribute( (String) key )!=null;
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
      return servletContext.getAttribute( (String) key );
   }

   public boolean isEmpty()
   {
      throw new UnsupportedOperationException();
   }

   public Set<String> keySet()
   {
      Set<String> keys = new HashSet<String>();
      Enumeration<String> names = servletContext.getAttributeNames();
      while ( names.hasMoreElements() )
      {
         keys.add( names.nextElement() );
      }
      return keys;
   }

   public Object put(String key, Object value)
   {
      Object result = servletContext.getAttribute(key);
      servletContext.setAttribute(key, value);
      return result;
   }

   public void putAll(Map<? extends String, ? extends Object> t)
   {
      throw new UnsupportedOperationException();
   }

   public Object remove(Object key)
   {
      Object result = servletContext.getAttribute( (String) key );
      servletContext.removeAttribute( (String) key );
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
