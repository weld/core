package org.jboss.webbeans.resources.spi;

import java.io.Serializable;


public interface Naming extends Serializable
{
   
   public static final String PROPERTY_NAME = Naming.class.getName();
   
   /**
    * Typed JNDI lookup
    * 
    * @param <T> The type
    * @param name The JNDI name
    * @param expectedType The excpected type
    * @return The object
    */
   @SuppressWarnings("unchecked")
   public <T> T lookup(String name, Class<? extends T> expectedType);

   public void bind(String key, Object value);
   
}
