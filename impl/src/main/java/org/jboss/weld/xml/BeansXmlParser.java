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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Simple parser for beans.xml
 * 
 * This class is NOT threadsafe, and should only be called in a single thread
 * 
 * @author Pete Muir
 */
public class BeansXmlParser
{

   public BeansXml parse(final URL beansXml)
   {
      if (beansXml == null)
      {
         throw new IllegalStateException(LOAD_ERROR, "unknown");
      }
      XMLReader xmlReader;
      try
      {
         xmlReader = XMLReaderFactory.createXMLReader();
      }
      catch (SAXException e)
      {
         throw new IllegalStateException(CONFIGURATION_ERROR, e);
      }
      try
      {
         InputSource source = new InputSource(beansXml.openStream());
         if (source.getByteStream().available() == 0)
         {
            // The file is just acting as a marker file
            return EMPTY_BEANS_XML;
         }
         BeansXmlHandler handler = new BeansXmlHandler(beansXml);
         xmlReader.setContentHandler(handler);
         xmlReader.setErrorHandler(handler);
         xmlReader.parse(source);
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

}
