package org.jboss.webbeans.ejb.spi;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve JPA persistence units
 * 
 * @author Pete Muir
 *
 */
public interface JpaResolver
{
   
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
