/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.discovery;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * The means by which beans are discovered on the classpath. This will only
 * discover simple beans - there is no EJB/Servlet/JPA integration.
 * 
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 */
public class SEWeldDiscovery
{

   private final Set<Class<?>> wbClasses;
   private final Set<URL> wbUrls;

   public SEWeldDiscovery()
   {
      this.wbClasses = new HashSet<Class<?>>();
      this.wbUrls = new HashSet<URL>();
   }

   public Iterable<Class<?>> discoverWeldClasses()
   {
      return Collections.unmodifiableSet(wbClasses);
   }

   public Collection<URL> discoverWeldXml()
   {
      return Collections.unmodifiableSet(wbUrls);
   }

   public Set<Class<?>> getWbClasses()
   {
      return wbClasses;
   }

   public Set<URL> getWbUrls()
   {
      return wbUrls;
   }

}
