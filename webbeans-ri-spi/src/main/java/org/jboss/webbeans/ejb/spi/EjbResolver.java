package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.webbeans.InjectionPoint;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve EJBs and JPA persistence units
 * 
 * @author Pete Muir
 * 
 */
public interface EjbResolver
{
   
   public static final String PROPERTY_NAME = EjbResolver.class.getName();
   
   /**
    * Resolve the JNDI name for the @EJB injection point
    * 
    * @param injectionPoint The injection point metadata
    * @return the JNDI name
    */
   public String resolveEjb(InjectionPoint injectionPoint);
   
   /**
    * Resolve the JNDI name for the @PersistenceContext injection point
    * 
    * @param injectionPoint The injection point metadata
    * @return the JNDI name
    */
   public String resolvePersistenceUnit(InjectionPoint injectionPoint);
   
   public Class<? extends Annotation> getEJBAnnotation();
   
   public Class<? extends Annotation> getPersistenceContextAnnotation();
   
}
