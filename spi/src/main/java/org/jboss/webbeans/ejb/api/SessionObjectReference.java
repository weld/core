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
package org.jboss.webbeans.ejb.api;

import java.io.Serializable;

/**
 * A serializable reference to a session object in the EJB container
 * 
 * @author Pete Muir
 */
public interface SessionObjectReference extends Serializable
{
   
   /**
    * Get the reference from the EJB container to the session object for the 
    * given business interface
    * 
    * @param <S>
    *           the type of the business interface
    * @param businessInterfaceType
    *           the type of the business interface
    * @return a reference
    * 
    * @throws IllegalStateException
    *           if the business interface is not a local business interface of 
    *           the session bean
    * @throws NoSuchEJBException
    *           if the session object has already been removed
    */
   public <S> S getBusinessObject(Class<S> businessInterfaceType);
   
   /**
    * Request the EJB container remove the stateful session object
    * 
    * @throws UnsupportedOperationException
    *            if the reference is not backed by a stateful session object
    * @throws NoSuchEJBException
    *            if the session object has already been removed           
    */
   public void remove();
   
}
