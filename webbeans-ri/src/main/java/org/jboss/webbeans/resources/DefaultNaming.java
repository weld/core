package org.jboss.webbeans.resources;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.webbeans.ExecutionException;

import org.jboss.webbeans.resources.spi.Naming;

public class DefaultNaming implements Naming
{
   
   private transient InitialContext initialContext;
   
   public DefaultNaming()
   {
      try 
      {
         this.initialContext = new InitialContext();
      }
      catch (NamingException e) 
      {
          throw new ExecutionException("Could not obtain InitialContext", e);
      }
   }



   public InitialContext getInitialContext()
   {
      return initialContext;
   }
   
   public void bind(String key, Object value)
   {
      try
      {
         initialContext.bind(key, value);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Cannot bind " + value + " to " + key, e);
      }
   }
   
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      Object instance;
      try
      {
         instance = initialContext.lookup(name);
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
   
}
