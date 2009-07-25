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
package org.jboss.webbeans.persistence.spi;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve JPA persistence units and discover entities
 * 
 * @author Pete Muir
 * 
 */
public interface JpaServices extends Service
{
	
   /**
    * Resolve the value for the given @PersistenceContext injection point
    * 
    * @param injectionPoint
    *           the injection point metadata
    * @return an instance of the entity manager
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with 
    *            @PersistenceContext, or, if the injection point is a method 
    *            that doesn't follow JavaBean conventions
    * @throws IllegalStateException
    *            if no suitable persistence units can be resolved for injection
    */
   public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint);
   
   /**
    * Resolve the value for the given @PersistenceUnit injection point
    * 
    * @param injectionPoint
    *           the injection point metadata
    * @return an instance of the entity manager
    * @throws IllegalArgumentException
    *            if the injection point is not annotated with 
    *            @PersistenceUnit, or, if the injection point is a method 
    *            that doesn't follow JavaBean conventions
    * @throws IllegalStateException
    *            if no suitable persistence units can be resolved for injection
    */
   public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint);
   
}
