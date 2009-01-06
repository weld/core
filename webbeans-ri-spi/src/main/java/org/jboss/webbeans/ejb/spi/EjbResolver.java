package org.jboss.webbeans.ejb.spi;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve EJBs and JPA persistence units
 * 
 * @author Pete Muir
 * 
 */
public interface EjbResolver
{
   /**
    * Resolve the EJB for the given parameters
    * 
    * @param name
    *           The logical name of the ejb reference within the declaring
    *           component's (java:comp/env) environment.
    * @param beanName
    *           The ejb-name of the Enterprise Java Bean to which this reference
    *           is mapped. Only applicable if the target EJB is defined within
    *           the same application or stand-alone module as the declaring
    *           component.
    * @param beanInterface
    *           Holds one of the following interface types of the target EJB : [
    *           Local business interface, Remote business interface, Local Home
    *           interface, Remote Home interface ]
    * @param mappedName
    *           The product specific name of the EJB component to which this ejb
    *           reference should be mapped. This mapped name is often a global
    *           JNDI name, but may be a name of any form. Application servers
    *           are not required to support any particular form or type of
    *           mapped name, nor the ability to use mapped names. The mapped
    *           name is product-dependent and often installation-dependent. No
    *           use of a mapped name is portable.
    * @return
    */
   public Object resolveEjb(String name, String beanName, Class<?> beanInterface, String mappedName);
   
   /**
    * Resolve the persistence unit for the given peristence unit name
    * 
    * @param persistenceUnitName
    *           the name of the persistence unit to resolve, if null, the
    *           default persistence unit for the application should be resolved
    * @return the resolved persistence unit
    */
   public Object resolvePersistenceUnit(String persistenceUnitName);
   
}
