package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.resources.spi.NamingContext;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve EJBs, Resources and JPA persistence units
 * 
 * @author Pete Muir
 * 
 */
public interface EjbResolver
{
   
   public static final String PROPERTY_NAME = EjbResolver.class.getName();
   
   /**
    * Resolve the value for the given @EJB injection point
    * 
    * @param injectionPoint the injection point metadata
    * @return an instance of the EJB
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with @EJB, or, if the 
    *            injection point is a method that doesn't follow JavaBean
    *            conventions
    * @throws IllegalStateException
    *            if no EJBs can be resolved for injection
    */
   public Object resolveEjb(InjectionPoint injectionPoint, NamingContext namingContext);
   
   /**
    * Resolve the value for the given @PersistenceContext injection point
    * 
    * @param injectionPoint the injection point metadata
    * @param namingContext the pluggable Web Beans JNDI lookup facility
    * @return an instance of the persistence unit
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with 
    *            @PersistenceContext, or, if the injection point is a method 
    *            that doesn't follow JavaBean conventions
    * @throws IllegalStateException
    *            if no suitable persistence units can be resolved for injection
    */
   public Object resolvePersistenceContext(InjectionPoint injectionPoint, NamingContext namingContext);
   
   /**
    * Resolve the value for the given @Resource injection point
    * 
    * @param injectionPoint the injection point metadata
    * @param namingContext the pluggable Web Beans JNDI lookup facility
    * @return an instance of the resource
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with @Resource, or, if 
    *            the injection point is a method that doesn't follow JavaBean 
    *            conventions
    * @throws IllegalStateException
    *            if no resource can be resolved for injection
    */
   public Object resolveResource(InjectionPoint injectionPoint, NamingContext namingContext);
   
   /**
    * Get the annotation which defines an @EJB injection point
    * 
    * @return the annotation which defines an @EJB injection point
    */
   public Class<? extends Annotation> getEJBAnnotation();
   
   /**
    * Get the annotation which defines a @PersistenceContext injection point
    * 
    * @return the annotation which defines a @PersistenceContext injection point
    */
   public Class<? extends Annotation> getPersistenceContextAnnotation();
   
   /**
    * Get the annotation which defines a @Resource injection point
    * 
    * @return the annotation which defines a @Resource injection point
    */
   public Class<? extends Annotation> getResourceAnnotation();
   
   
}
