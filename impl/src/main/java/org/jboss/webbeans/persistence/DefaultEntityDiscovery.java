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

package org.jboss.webbeans.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.persistence.spi.EntityDiscovery;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Pete Muir
 * 
 */
public class DefaultEntityDiscovery implements EntityDiscovery
{
   
   private static final Log log = Logging.getLog(DefaultEntityDiscovery.class);
   
   private final ServiceRegistry serviceRegistry;
   private Set<Class<?>> entitiesFromAnnotations;
   private Set<Class<?>> entitiesFromOrmXml;
   private Set<Class<?>> entitiesFromPersistenceXml;
   
   public DefaultEntityDiscovery(ServiceRegistry serviceRegistry)
   {
      this.serviceRegistry = serviceRegistry;
      
   }
   
   public void initialize()
   {
      this.entitiesFromAnnotations = new HashSet<Class<?>>();
      this.entitiesFromOrmXml = new HashSet<Class<?>>();
      this.entitiesFromPersistenceXml = new HashSet<Class<?>>();
      PersistenceApiAbstraction jpaApiAbstraction = serviceRegistry.get(PersistenceApiAbstraction.class);
      // TODO process persistence.xml
      for (Class<?> clazz : serviceRegistry.get(WebBeanDiscovery.class).discoverWebBeanClasses())
      {
         if (clazz.isAnnotationPresent(jpaApiAbstraction.ENTITY_CLASS) || clazz.isAnnotationPresent(jpaApiAbstraction.EMBEDDABLE_CLASS) || clazz.isAnnotationPresent(jpaApiAbstraction.MAPPED_SUPERCLASS_CLASS))
         {
            entitiesFromAnnotations.add(clazz);
         }
      }
      for (URL url : serviceRegistry.get(ResourceLoader.class).getResources("META-INF/orm.xml"))
      {
         try
         {
            parseOrmXml(getRootElementSafely(url));
         }
         catch (Exception e)
         {
            log.warn("Error parsing {0}, entities defined there will be detected as simple beans", e, url.toString());
         }
      }
   }
   
   @SuppressWarnings("unchecked")
   private void parseOrmXml(Element root) 
   {
       String packagePrefix = "";
       
       Element pkg = root.element("package");
       if (pkg!=null) 
       {
           packagePrefix = pkg.getTextTrim() + ".";
       }
       ResourceLoader resourceLoader = serviceRegistry.get(ResourceLoader.class);
       for (Element entity: (List<Element>) root.elements("entity")) 
       {
           String className = packagePrefix + entity.attribute("class").getText();
           entitiesFromOrmXml.add(resourceLoader.classForName(className));
       }
       for (Element entity: (List<Element>) root.elements("mapped-superclass")) 
       {
           String className = packagePrefix + entity.attribute("class").getText();
           entitiesFromOrmXml.add(resourceLoader.classForName(className));
       }
   }

   public Collection<Class<?>> discoverEntitiesFromAnnotations()
   {
      if (entitiesFromAnnotations == null)
      {
         initialize();
      }
      return Collections.unmodifiableCollection(entitiesFromAnnotations);
   }

   public Collection<Class<?>> discoverEntitiesFromPersistenceUnits()
   {
      if (entitiesFromPersistenceXml == null)
      {
         initialize();
      }
      return Collections.unmodifiableCollection(entitiesFromPersistenceXml);
   }

   public Collection<Class<?>> discoverEntitiesFromXml()
   {
      if (entitiesFromOrmXml == null)
      {
         initialize();
      }
      return Collections.unmodifiableCollection(entitiesFromOrmXml);
   }
   
   /**
    * Parses an XML document safely, as to not resolve any external DTDs
    * @throws IOException 
    */
   public static Element getRootElementSafely(URL url)  throws DocumentException, IOException
   {
      InputStream stream = url.openStream();
      SAXReader saxReader = new SAXReader();
      saxReader.setEntityResolver(new NullEntityResolver());
      saxReader.setMergeAdjacentText(true);
      return saxReader.read(stream).getRootElement();       
   }
   
   private static class NullEntityResolver implements EntityResolver 
   {
      private static final byte[] empty = new byte[0];

      public InputSource resolveEntity(String systemId, String publicId) throws SAXException, IOException 
      {
         return new InputSource(new ByteArrayInputStream(empty));
      }

   }

}
