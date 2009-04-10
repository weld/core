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

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.ejb.api.SessionObjectReference;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * resolve EJB and discover EJBs
 * 
 * @author Pete Muir
 * 
 */
public interface EjbServices extends Service
{
   
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
   
    */
   public Object resolveEjb(InjectionPoint injectionPoint);
   
   /**
    * Request a reference to an EJB session object from the container. If the
    * EJB being resolved is a stateful session bean, the container should ensure
    * the session bean is created before this method returns.
    * 
    * @param ejbDescriptor the ejb to resolve
    * @return a reference to the session object
    */
   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor);
   
   /**
    * Resolve a remote EJB reference. At least one of the parameters will not be
    * null.
    * 
    * @param jndiName the JNDI name
    * @param mappedName the mapped name
    * @param ejbLink the EJB link name
    * @return the remote EJB reference
    * @throws IllegalStateException
    *            if no EJBs can be resolved for injection
    * @throws IllegalArgumentException
    *            if jndiName, mappedName and ejbLink are null
    */
   public Object resolveRemoteEjb(String jndiName, String mappedName, String ejbLink);
   
   /**
    * Gets a descriptor for each EJB in the application
    * 
    * @return the EJB descriptors
    */
   public Iterable<EjbDescriptor<?>> discoverEjbs();
   
}
