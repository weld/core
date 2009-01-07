package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.persistence.EntityManagerFactory;
import javax.webbeans.InjectionPoint;

import org.jboss.webbeans.resources.spi.Naming;

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
   public Object resolveEjb(InjectionPoint injectionPoint, Naming naming);
   
   /**
    * Resolve the JNDI name for the @PersistenceContext injection point
    * 
    * @param injectionPoint The injection point metadata
    * @return the JNDI name
    */
   public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint, Naming naming);
   
   public Class<? extends Annotation> getEJBAnnotation();
   
   public Class<? extends Annotation> getPersistenceContextAnnotation();
   
}
