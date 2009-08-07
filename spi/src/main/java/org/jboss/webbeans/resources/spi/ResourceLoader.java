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

package org.jboss.webbeans.resources.spi;

import java.net.URL;
import java.util.Collection;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * Resource loading/class creation services for Web Beans. By default an
 * implementation which uses the Thread Context ClassLoader if available, 
 * otherwise the classloading of the implementation is used 
 * 
 * @author Pete Muir
 *
 */
public interface ResourceLoader extends Service
{
   // Name of the resource loader
   public static final String PROPERTY_NAME = ResourceLoader.class.getName();
   
   /**
    * Creates a class from a given FQCN
    * 
    * @param name The name of the clsas
    * @return The class
    */
   public Class<?> classForName(String name);
   
   /**
    * Gets a resource as a URL by name
    * 
    * @param name The name of the resource
    * @return An URL to the resource
    */
   public URL getResource(String name);
   
   /**
    * Gets resources as URLs by name
    * 
    * @param name The name of the resource
    * @return references to the URLS
    */
   public Collection<URL> getResources(String name);
   
}
