package org.jboss.webbeans.ws.spi;


import org.jboss.webbeans.bootstrap.api.Service;

/**
 * A container should implement this interface to allow Web Beans to resolve Web
 * Services
 * 
 * @author Pete Muir
 * 
 */
public interface WebServices extends Service
{
   
   /**
    * Resolve the value for the given JNDI name and mapped name
    * 
    * @param injectionPoint
    *           the injection point metadata
    * @return an instance of the resource
    * @throws IllegalStateException
    *            if no resource can be resolved for injection
    */
   public Object resolveResource(String jndiName, String mappedName);
   
}