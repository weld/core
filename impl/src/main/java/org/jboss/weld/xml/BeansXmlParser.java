/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.xml;

import static org.jboss.weld.bootstrap.spi.BeansXml.EMPTY_BEANS_XML;
import static org.jboss.weld.logging.messages.XmlMessage.CONFIGURATION_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.LOAD_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.PARSING_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple parser for beans.xml
 * 
 * This class is NOT threadsafe, and should only be called in a single thread
 * 
 * @author Pete Muir
 */
public class BeansXmlParser
{
   
   private static final InputSource[] EMPTY_INPUT_SOURCE_ARRAY = new InputSource[0];

   public BeansXml parse(final URL beansXml)
   {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(true);
      factory.setNamespaceAware(true);
      if (beansXml == null)
      {
         throw new IllegalStateException(LOAD_ERROR, "unknown");
      }
      SAXParser parser;
      try
      {
         parser = factory.newSAXParser();
      }
      catch (SAXException e)
      {
         throw new IllegalStateException(CONFIGURATION_ERROR, e);
      }
      catch (ParserConfigurationException e)
      {
         throw new IllegalStateException(CONFIGURATION_ERROR, e);
      }
      InputStream beansXmlInputStream = null;
      try
      {
         beansXmlInputStream = beansXml.openStream();
         InputSource source = new InputSource(beansXmlInputStream);
         if (source.getByteStream().available() == 0)
         {
            // The file is just acting as a marker file
            return EMPTY_BEANS_XML;
         }
         BeansXmlHandler handler = new BeansXmlHandler(beansXml);
         
         try
         {
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", loadXsds());
         }
         catch (IllegalArgumentException e)
         {
            // No op, we just don't validate the XML
         }
         
         parser.parse(source, handler);
         
         return handler.createBeansXml();
      }
      catch (IOException e)
      {
         throw new IllegalStateException(LOAD_ERROR, e, beansXml);
      }
      catch (SAXException e)
      {
         throw new IllegalStateException(PARSING_ERROR, beansXml, e);
      }
      finally
      {
         if (beansXmlInputStream != null)
         {
            try
            {
               beansXmlInputStream.close();
            }
            catch (IOException e)
            {
               throw new IllegalStateException(e);
            }
         }
      }
   }

   public BeansXml parse(Iterable<URL> urls)
   {
      List<Metadata<String>> alternativeStereotypes = new ArrayList<Metadata<String>>();
      List<Metadata<String>> alternativeClasses = new ArrayList<Metadata<String>>();
      List<Metadata<String>> decorators = new ArrayList<Metadata<String>>();
      List<Metadata<String>> interceptors = new ArrayList<Metadata<String>>();
      List<Metadata<Filter>> includes = new ArrayList<Metadata<Filter>>();
      List<Metadata<Filter>> excludes = new ArrayList<Metadata<Filter>>();
      for (URL url : urls)
      {
         BeansXml beansXml = parse(url);
         alternativeStereotypes.addAll(beansXml.getEnabledAlternativeStereotypes());
         alternativeClasses.addAll(beansXml.getEnabledAlternativeClasses());
         decorators.addAll(beansXml.getEnabledDecorators());
         interceptors.addAll(beansXml.getEnabledInterceptors());
         includes.addAll(beansXml.getScanning().getIncludes());
         excludes.addAll(beansXml.getScanning().getExcludes());
      }
      return new BeansXmlImpl(alternativeClasses, alternativeStereotypes, decorators, interceptors, new ScanningImpl(includes, excludes));
   }
   
   private static InputSource[] loadXsds()
   {
      List<InputSource> xsds = new ArrayList<InputSource>();
      // The Weld xsd
      InputSource weldXsd = loadXsd("beans_1_1.xsd", BeansXmlParser.class.getClassLoader());
      // The CDI Xsd
      InputSource cdiXsd = loadXsd("beans_1_0.xsd", BeanManager.class.getClassLoader()); 
      if (weldXsd != null)
      {
         xsds.add(weldXsd);         
      }
      if (cdiXsd != null)
      {
         xsds.add(cdiXsd);
      }
      return xsds.toArray(EMPTY_INPUT_SOURCE_ARRAY);
   }
   
   
   private static InputSource loadXsd(String name, ClassLoader classLoader)
   {
      InputStream in = classLoader.getResourceAsStream(name);
      if (in == null)
      {
         return null;
      }
      else
      {
         return new InputSource(in);
      }
   }


}
