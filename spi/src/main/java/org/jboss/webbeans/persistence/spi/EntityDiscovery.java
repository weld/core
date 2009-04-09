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

import java.util.Collection;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * A container should implement this interface to replace portions or all
 * of the built in entity discovery in Web Beans.
 * 
 * The built in discovery considers @Entity, parses any META-INF/orm.xml
 * and META-INF/persistence.xml. 
 * 
 * @author Pete Muir
 * 
 */
public interface EntityDiscovery extends Service
{
	/**
	 * Discover any entities defined using annotations
	 * 
	 * @return an iteration of the entity classes found
	 */
	public Collection<Class<?>> discoverEntitiesFromAnnotations();
	
	/**
	 * Discover any entities defined using XML
	 * 
	 * @return an iteration of the entity classes found
	 */
	public Collection<Class<?>> discoverEntitiesFromXml();
	
	/**
	 * Discover any extra entities defined using persistence unit configuration
	 * 
	 * @return an iteration of the entity classes found
	 */
	public Collection<Class<?>> discoverEntitiesFromPersistenceUnits();
   
}
