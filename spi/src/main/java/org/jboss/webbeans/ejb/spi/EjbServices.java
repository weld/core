/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ejb.api.EjbReference;
import org.jboss.webbeans.resources.spi.NamingContext;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve EJBs, Resources and JPA persistence units
 * 
 * @author Pete Muir
 * 
 */
public interface EjbServices
{
   
   public static final String PROPERTY_NAME = EjbServices.class.getName();
   
   /**
    * Resolve the value for the given @EJB injection point
    * 
    * @param injectionPoint
    *           the injection point metadata
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
    * @param injectionPoint
    *           the injection point metadata
    * @param namingContext
    *           the pluggable Web Beans JNDI lookup facility
    * @return an instance of the persistence unit
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with
    * @PersistenceContext, or, if the injection point is a method that doesn't
    *                      follow JavaBean conventions
    * @throws IllegalStateException
    *            if no suitable persistence units can be resolved for injection
    */
   public Object resolvePersistenceContext(InjectionPoint injectionPoint, NamingContext namingContext);
   
   /**
    * Resolve the value for the given @Resource injection point
    * 
    * @param injectionPoint
    *           the injection point metadata
    * @param namingContext
    *           the pluggable Web Beans JNDI lookup facility
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
    * Request an EJB instance from the container
    * 
    * @param <T> the type of the bean class
    * @param ejbDescriptor the ejb to resolve
    * @param namingContext the pluggable Web Beans JNDI lookup facility
    * @return a reference to the EJB
    */
   public <T> EjbReference<T> resolveEJB(EjbDescriptor<T> ejbDescriptor, NamingContext namingContext);
   
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
