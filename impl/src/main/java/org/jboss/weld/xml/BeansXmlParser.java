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

import static org.jboss.weld.logging.messages.XmlMessage.CONFIGURATION_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.LOAD_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.PARSING_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.weld.manager.EnabledClasses;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Simple parser for beans.xml
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 */
public class BeansXmlParser
{
   
   public static final String NAMESPACE_URI = "http://java.sun.com/xml/ns/javaee";
   
   private final Iterable<URL> beansXmls;
   private final ResourceLoader resourceLoader;

   public BeansXmlParser(ResourceLoader resourceLoader, Iterable<URL> beansXmls)
   {
      this.beansXmls = beansXmls;
      this.resourceLoader = resourceLoader;
   }

   public EnabledClasses parse()
   {
      DocumentBuilder documentBuilder = createDocumentBuilder();
      MergedElements mergedElements = new MergedElements();
      for (URL beansXml : beansXmls)
      {
         if (!isBeansXmlOK(beansXml))
         {
            continue;
         }
         Document document = loadDocument(documentBuilder, beansXml);
         if (document.getDocumentElement().getNamespaceURI() == null)
         {
            mergedElements.merge(beansXml, document, "*");
         }
         else
         {
            mergedElements.merge(beansXml, document, NAMESPACE_URI);
         }
         
      }
      List<Class<?>> enabledAlternativeClasses = new ArrayList<Class<?>>();
      List<Class<? extends Annotation>> enabledAlternativeStereotypes = new ArrayList<Class<? extends Annotation>>();
      List<Class<?>> enabledDecoratorClasses = new ArrayList<Class<?>>();
      List<Class<?>> enabledInterceptorClasses = new ArrayList<Class<?>>();
      for (BeansXmlElement element : mergedElements.getAlternativeClassElements())
      {
         enabledAlternativeClasses.addAll(element.getClasses(resourceLoader));
      }
      for (BeansXmlElement element : mergedElements.getAlternativeStereotypeElements())
      {
         enabledAlternativeStereotypes.addAll(element.<Annotation>getClasses(resourceLoader));
      }
      for (BeansXmlElement element : mergedElements.getDecoratorsElements())
      {
         enabledDecoratorClasses.addAll(element.getClasses(resourceLoader));
      }
      for (BeansXmlElement element : mergedElements.getInterceptorsElements())
      {
         enabledInterceptorClasses.addAll(element.getClasses(resourceLoader));
      }
      return new EnabledClasses(enabledAlternativeStereotypes, enabledAlternativeClasses, enabledDecoratorClasses, enabledInterceptorClasses);
   }

   private Document loadDocument(DocumentBuilder documentBuilder, URL beansXml)
   {
      Document document;
      InputStream in = null;
      try
      {
         in = beansXml.openStream();
         document = documentBuilder.parse(in);
         document.normalize();
      }
      catch (SAXException e)
      {
         throw new WeldXmlException(PARSING_ERROR, e, beansXml.toString());
      }
      catch (IOException e)
      {
         throw new WeldXmlException(LOAD_ERROR, e, beansXml.toString());
      }
      finally
      {
         closeStream(in);
      }
      return document;
   }

   private void closeStream(InputStream in)
   {
      if (in == null)
      {
         return;
      }
      try
      {
         in.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private boolean isBeansXmlOK(URL beansXml)
   {
      if (beansXml == null)
      {
         throw new WeldXmlException(LOAD_ERROR, "URL: null");
      }
      InputStream in = null;
      try
      {
         in = beansXml.openStream();
         return in.available() > 0;
      }
      catch (IOException e)
      {
         throw new WeldXmlException(LOAD_ERROR, e, beansXml.toString());
      }
      finally
      {
         closeStream(in);
      }
   }

   private DocumentBuilder createDocumentBuilder()
   {
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         return factory.newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         throw new WeldXmlException(CONFIGURATION_ERROR, e);
      }
   }

}
