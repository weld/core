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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.util.dom.NodeListIterable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
class BeansXmlElement
{
   
   private final URL url;
   private final Element element;
   private final String localName;
   private final String namespaceURI;

   BeansXmlElement(URL url, Element element, String localName, String namespaceURI)
   {
      this.url = url;
      this.element = element;
      this.localName = localName;
      this.namespaceURI = namespaceURI;
   }

   private static String getClassNameFromElement(Element element)
   {
      if (element.getChildNodes().getLength() == 1 && element.getChildNodes().item(0) instanceof Text)
      {
         String className = ((Text) element.getChildNodes().item(0)).getData();
         return className;
      }
      return null;
   }

   public List<String> getClassNames()
   {
      List<String> classes = new ArrayList<String>();
      for (Node child : new NodeListIterable(element.getElementsByTagNameNS(namespaceURI, localName)))
      {
         // Unsafe looking cast is actually safe as the NodeList only contains Elements
         String className = getClassNameFromElement((Element) child);
         if (className != null)
         {
            classes.add(className);
         }
      }
      return classes;
   }

   @Override
   public String toString()
   {
      return element + " in " + url;
   }
   
   

}