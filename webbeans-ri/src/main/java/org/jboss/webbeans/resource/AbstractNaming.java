package org.jboss.webbeans.resource;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.webbeans.ExecutionException;

import org.jboss.webbeans.resources.spi.Naming;

public abstract class AbstractNaming implements Naming
{
   
   public abstract Context getContext();
   
   /**
    * Binds in item to JNDI
    * 
    * @param key The key to bind under
    * @param value The value to bind
    */
   public void bind(String key, Object value)
   {
      try
      {
         List<String> parts = splitIntoContexts(key);
         Context context = getContext();
         for (int i = 0; i < parts.size() - 1; i++)
         {
            context = (Context) context.lookup(parts.get(i));
         }
         context.bind(parts.get(parts.size() - 1), value);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Cannot bind " + value + " to " + key, e);
      }
   }

   /**
    * Lookup an item from JNDI
    * 
    * @param name The key
    * @param expectedType The expected return type
    * @return The found item
    */
   @SuppressWarnings("unchecked")
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      Object instance;
      try
      {
         instance = getContext().lookup(name);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Cannot lookup " + name, e);
      }
      try
      {
         return (T) instance;
      }
      catch (ClassCastException e)
      {
         throw new ExecutionException(instance + " not of expected type " + expectedType, e);
      }
   }
   
   private static List<String> splitIntoContexts(String key)
   {
      List<String> parts = new ArrayList<String>();
      for (String part : key.split("/"))
      {
         parts.add(part);
      }
      return parts;
   }
   
}