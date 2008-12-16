/**
 * 
 */
package org.jboss.webbeans.bootstrap.spi;

public interface BusinessInterfaceDescriptor<T>
{

   /**
    * Gets the business interface class
    */
   public Class<T> getInterface();

   /**
    * Gets the JNDI name under which the EJB is registered
    * 
    * @return The JNDI name
    */
   public String getJndiName();
   
}