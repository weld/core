package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.webbeans.DefinitionException;
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
    * Resolve the value for the given
    * 
    * @EJB injection point
    * 
    * @param injectionPoint
    *           The injection point metadata
    * @return the JNDI name
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with
    * @EJB
    * @throws DefinitionException
    *            if the injection point is not suitable for injection
    * @throws IllegalStateException
    *            if no EJBs can be resolved for injection
    */
   public Object resolveEjb(InjectionPoint injectionPoint, Naming naming);
   
   /**
    * Resolve the value for the given
    * 
    * @PersistenceContext injection point
    * 
    * @param injectionPoint
    *           The injection point metadata
    * @return the JNDI name
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with
    * @PersistenceContext
    * @throws UnsupportedOperationException
    *            if the injection point is annotated
    * @PersistenceContext(EXTENTED)
    * @throws IllegalStateException
    *            if no suitable persistence units can be resolved for injection
    */
   public Object resolvePersistenceUnit(InjectionPoint injectionPoint, Naming naming);
   
   /**
    * Get the annotation which defines an
    * 
    * @EJB injection point
    * 
    * @return the annotation which defines an
    * @EJB injection point
    */
   public Class<? extends Annotation> getEJBAnnotation();
   
   /**
    * Get the annoation which defines a
    * 
    * @PersistenceContext injection point
    * 
    * @return the annoation which defines a
    * @PersistenceContext injection point
    */
   public Class<? extends Annotation> getPersistenceContextAnnotation();
   
}
