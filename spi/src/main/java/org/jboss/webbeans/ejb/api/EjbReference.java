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
 * A serializable reference to an EJB. For an SFSB the same state must be 
 * returned for each call to {@link #get(Class)}
 * 
 * @author Pete Muir
 * @param <T>
 *           The EJB bean class
 */
public interface EjbReference<T> extends Serializable
{
   
   /**
    * Get the reference to the EJB for the given business interfaces
    * 
    * The reference may be lazily instantiated; if Web Beans wishes to eagerly
    * instantiated the bean it will call {@link #create()}.
    * 
    * @param <S>
    *           the type of the business interface
    * @param businessInterfaceType
    *           the type of the business interface
    * @return a reference
    */
   public <S> S get(Class<S> businessInterfaceType);
   
   /**
    * Request the SFSB backing this reference is removed
    * 
    * @throws UnsupportedOperationException
    *            if the reference is backed by an SLSB
    */
   public void remove();
   
   /**
    * Request that the SFSB backing this reference is instantiated, and any
    * @PostConstruct lifecycle callbacks are executed
    * 
    * @throws UnsupportedOperationException
    *            if the reference is backed by an SLSB
    */
   public void create();
   
}
