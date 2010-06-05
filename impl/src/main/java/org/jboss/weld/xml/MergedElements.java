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

import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_ALTERNATIVES;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_DECORATORS;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_INTERCEPTORS;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.logging.messages.XmlMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Nicklas Karlsson
 * 
 */
public class MergedElements
{
   private List<BeansXmlElement> alternativesElements = new ArrayList<BeansXmlElement>();
   private List<BeansXmlElement> decoratorsElements = new ArrayList<BeansXmlElement>();
   private List<BeansXmlElement> interceptorsElements = new ArrayList<BeansXmlElement>();


   public void merge(URL url, Document beansXmlDocument, String namespace)
   {
      Element documentElement = beansXmlDocument.getDocumentElement();
      alternativesElements.addAll(findNamedElement(url, documentElement, namespace, "alternatives", MULTIPLE_ALTERNATIVES));
      interceptorsElements.addAll(findNamedElement(url, documentElement, namespace, "interceptors", MULTIPLE_INTERCEPTORS));
      decoratorsElements.addAll(findNamedElement(url, documentElement, namespace, "decorators", MULTIPLE_DECORATORS));
   }

   private List<BeansXmlElement> findNamedElement(URL url, Element beans, String namespace, String name, XmlMessage multipleViolationMessage)
   {
      List<BeansXmlElement> elements = new ArrayList<BeansXmlElement>();
      NodeList nodeList = beans.getElementsByTagNameNS(namespace, name);
      if (nodeList.getLength() > 1)
      {
         throw new WeldXmlException(multipleViolationMessage);
      }
      else if (nodeList.getLength() == 1)
      {
         BeansXmlElement element = new BeansXmlElement(url, (Element) nodeList.item(0));
         elements.add(element);
      }
      return elements;
   }

   
   public List<BeansXmlElement> getAlternativesElements()
   {
      return alternativesElements;
   }

   public List<BeansXmlElement> getDecoratorsElements()
   {
      return decoratorsElements;
   }

   public List<BeansXmlElement> getInterceptorsElements()
   {
      return interceptorsElements;
   }

}
