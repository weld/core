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

import static java.util.Collections.emptyList;
import static org.jboss.weld.bootstrap.spi.BeansXml.EMPTY_BEANS_XML;
import static org.jboss.weld.logging.messages.XmlMessage.LOAD_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_ALTERNATIVES;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_DECORATORS;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_INTERCEPTORS;
import static org.jboss.weld.logging.messages.XmlMessage.PARSING_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.logging.messages.XmlMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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

   public BeansXml parse(final URL beansXml)
   {
      Document document = new BeansXmlDocumentLoader(beansXml)
      {

         @Override
         protected Document loadDocument(InputStream in)
         {
            if (isDocumentEmpty(in))
            {
               return null;
            }
            try
            {
               Document document = createDocumentBuilder().parse(in);
               document.normalize();
               return document;
            }
            catch (SAXException e)
            {
               throw new org.jboss.weld.exceptions.IllegalStateException(PARSING_ERROR, e, beansXml.toString());
            }
            catch (IOException e)
            {
               throw new org.jboss.weld.exceptions.IllegalStateException(LOAD_ERROR, e, beansXml.toString());
            }
         }
      }.run();

      if (document == null)
      {
         return EMPTY_BEANS_XML;
      }
      else
      {
         if (document.getDocumentElement().getNamespaceURI() == null)
         {
            return parseDocument(beansXml, document.getDocumentElement(), "*");
         }
         else
         {
            return parseDocument(beansXml, document.getDocumentElement(), NAMESPACE_URI);
         }
      }
   }

   private BeansXml parseDocument(URL url, Element documentElement, String namespaceURI)
   {
      return new BeansXmlImpl(
            findClassNames(url, documentElement, namespaceURI, "alternatives", "class", MULTIPLE_ALTERNATIVES),
            findClassNames(url, documentElement, namespaceURI, "alternatives", "stereotype", MULTIPLE_ALTERNATIVES),
            findClassNames(url, documentElement, namespaceURI, "decorators", "class", MULTIPLE_DECORATORS),
            findClassNames(url, documentElement, namespaceURI, "interceptors", "class", MULTIPLE_INTERCEPTORS)
         ); 
   }
   
   private static List<String> findClassNames(URL url, Element beans, String namespaceURI, String localGroupName, String localName, XmlMessage multipleViolationMessage)
   {
      NodeList nodeList = beans.getElementsByTagNameNS(namespaceURI, localGroupName);
      if (nodeList.getLength() > 1)
      {
         throw new DefinitionException(multipleViolationMessage);
      }
      else if (nodeList.getLength() == 1)
      {
         return new BeansXmlElement(url, (Element) nodeList.item(0), localName, namespaceURI).getClassNames();
      }
      else
      {
         return emptyList();
      }
   }

   public BeansXml parse(Iterable<URL> urls)
   {
      List<String> alternativeStereotypes = new ArrayList<String>();
      List<String> alternativeClasses = new ArrayList<String>();
      List<String> decorators = new ArrayList<String>();
      List<String> interceptors = new ArrayList<String>();
      for (URL url : urls)
      {
         BeansXml beansXml = parse(url);
         alternativeStereotypes.addAll(beansXml.getEnabledAlternativeStereotypes());
         alternativeClasses.addAll(beansXml.getEnabledAlternativeClasses());
         decorators.addAll(beansXml.getEnabledDecorators());
         interceptors.addAll(beansXml.getEnabledInterceptors());
      }
      return new BeansXmlImpl(alternativeClasses, alternativeStereotypes, decorators, interceptors);
   }

}
