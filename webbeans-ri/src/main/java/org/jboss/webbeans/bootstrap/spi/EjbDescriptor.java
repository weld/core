package org.jboss.webbeans.bootstrap.spi;

import java.lang.reflect.Method;
import java.util.Iterator;

public interface EjbDescriptor<T>
{
   /**
    * @return The EJB Bean class
    */
   public Class<T> getType();

   /**
    * @return The JNDI name under which the EJB is registered
    */
   public String getJndiName();

   /**
    * @return The local interfaces of the EJB 
    */
   public Iterator<Class<?>> getLocalInterfaces();

   /**
    * @return The remove methods of the EJB
    */
   public Iterator<Method> getRemoveMethods();
   
}
