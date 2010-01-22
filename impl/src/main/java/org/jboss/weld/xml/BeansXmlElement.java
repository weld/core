/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.messages.XmlMessage.CANNOT_LOAD_CLASS;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.dom.NodeListIterable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * 
 * @author Nicklas Karlsson
 * 
 */
public class BeansXmlElement
{
   private URL file;
   private Element element;

   private BeansXmlElement(URL file, Element element)
   {
      super();
      this.file = file;
      this.element = element;
   }

   private String getClassNameFromNode(Node node)
   {
      if (node instanceof Element)
      {
         if (node.getChildNodes().getLength() == 1 && node.getChildNodes().item(0) instanceof Text)
         {
            String className = ((Text) node.getChildNodes().item(0)).getData();
            return className;
         }
      }
      return null;
   }

   public static BeansXmlElement of(URL file, Node element)
   {
      return new BeansXmlElement(file, (Element) element);
   }

   public List<Class<?>> getClasses(ResourceLoader resourceLoader)
   {
      List<Class<?>> classes = new ArrayList<Class<?>>();
      for (Node child : new NodeListIterable(element.getChildNodes()))
      {
         String className = getClassNameFromNode(child);
         if (className == null)
         {
            continue;
         }
         try
         {
            classes.add(resourceLoader.classForName(className));
         }
         catch (ResourceLoadingException e)
         {
            throw new WeldXmlException(CANNOT_LOAD_CLASS, className, file);
         }
      }
      return classes;
   }

   public URL getFile()
   {
      return file;
   }

   public Element getElement()
   {
      return element;
   }

   @Override
   public String toString()
   {
      return "File: " + getFile() + "; Node: " + getElement();
   }

}