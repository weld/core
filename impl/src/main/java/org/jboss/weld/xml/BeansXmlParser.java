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
package org.jboss.weld.xml;

import static org.jboss.weld.logging.messages.XmlMessage.CANNOT_LOAD_CLASS;
import static org.jboss.weld.logging.messages.XmlMessage.CONFIGURATION_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.LOAD_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_ALTERNATIVES;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_DECORATORS;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_INTERCEPTORS;
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

import org.jboss.weld.DeploymentException;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.dom.NodeListIterable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Simple parser for beans.xml
 * 
 * @author Pete Muir
 *
 */
public class BeansXmlParser
{
   
   private static class XmlElement
   {
      private URL file;
      private Element element;
      
      public XmlElement(URL file, Element element)
      {
         super();
         this.file = file;
         this.element = element;
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
   
   private final Iterable<URL> beansXml;
   private final ResourceLoader resourceLoader;
   
   private List<Class<? extends Annotation>> enabledPolicyStereotypes;
   private List<Class<?>> enabledPolicyClasses;
   private List<Class<?>> enabledDecoratorClasses;
   private List<Class<?>> enabledInterceptorClasses;
   
   public List<Class<?>> getEnabledPolicyClasses()
   {
      return enabledPolicyClasses;
   }
   
   public List<Class<? extends Annotation>> getEnabledPolicyStereotypes()
   {
      return enabledPolicyStereotypes;
   }
   
   public List<Class<?>> getEnabledDecoratorClasses()
   {
      return enabledDecoratorClasses;
   }
   
   public List<Class<?>> getEnabledInterceptorClasses()
   {
      return enabledInterceptorClasses;
   }
   
   public BeansXmlParser(ResourceLoader resourceLoader, Iterable<URL> beansXml)
   {
      this.beansXml = beansXml;
      this.resourceLoader = resourceLoader;
   }
   
   public void parse()
   {
      DocumentBuilder documentBuilder;
      try
      {
         documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         throw new WeldXmlException(CONFIGURATION_ERROR, e);
      }
      List<XmlElement> policiesElements = new ArrayList<XmlElement>(); 
      List<XmlElement> decoratorsElements = new ArrayList<XmlElement>(); 
      List<XmlElement> interceptorsElements = new ArrayList<XmlElement>(); 
      for (URL url : beansXml)
      {
         InputStream is;
         boolean fileHasContents;
         try
         {
            is = url.openStream();
            fileHasContents = is.available() > 0;
         }
         catch (IOException e)
         {
            throw new WeldXmlException(LOAD_ERROR, e, url.toString());
         }
         if (fileHasContents)
         {
            Document document;
            try
            {
               document = documentBuilder.parse(is);
               document.normalize();
            }
            catch (SAXException e)
            {
               throw new DeploymentException(PARSING_ERROR, e, url.toString());
            }
            catch (IOException e)
            {
               throw new DeploymentException(LOAD_ERROR, e, url.toString());
            }
            Element beans = document.getDocumentElement();
            for (Node child : new NodeListIterable(beans.getChildNodes()))
            {
               if (child instanceof Element && "alternatives".equals(child.getNodeName()))
               {
                  policiesElements.add(new XmlElement(url, (Element) child));
               }
               if (child instanceof Element && "interceptors".equals(child.getNodeName()))
               {
                  interceptorsElements.add(new XmlElement(url, (Element) child));
               }

               if (child instanceof Element && "decorators".equals(child.getNodeName()))
               {
                  decoratorsElements.add(new XmlElement(url, (Element) child));
               }
            }
         }
      }
      
      if (policiesElements.size() > 1)
      {
         throw new DeploymentException(MULTIPLE_ALTERNATIVES, policiesElements);
      }
      else if (policiesElements.size() == 1)
      {
         enabledPolicyStereotypes = new ArrayList<Class<? extends Annotation>>();
         enabledPolicyClasses = new ArrayList<Class<?>>();
         processPolicyElement(resourceLoader, policiesElements.get(0), enabledPolicyClasses, enabledPolicyStereotypes);
      }
      
      if (decoratorsElements.size() > 1)
      {
         throw new DeploymentException(MULTIPLE_DECORATORS, decoratorsElements);
      }
      else if (decoratorsElements.size() == 1)
      {
         enabledDecoratorClasses = new ArrayList<Class<?>>();
         enabledDecoratorClasses.addAll(processElement(resourceLoader, decoratorsElements.get(0)));
      }
      
      if (interceptorsElements.size() > 1)
      {
         throw new DeploymentException(MULTIPLE_INTERCEPTORS, interceptorsElements);
      }
      else if (interceptorsElements.size() == 1)
      {
         enabledInterceptorClasses = new ArrayList<Class<?>>();
         enabledInterceptorClasses.addAll(processInterceptorElement(resourceLoader, interceptorsElements.get(0)));
      }
      
   }
   
   private static void processPolicyElement(ResourceLoader resourceLoader, XmlElement element, List<Class<?>> enabledPolicyClasses, List<Class<? extends Annotation>> enabledPolicyStereotypes)
   {
      for (Node child : new NodeListIterable(element.getElement().getChildNodes()))
      {
         String className = processNode(child);
         if (className != null)
         {
            try
            {
               Class<?> clazz = resourceLoader.classForName(className);
               if (clazz.isAnnotation())
               {
                  enabledPolicyStereotypes.add(clazz.asSubclass(Annotation.class));
               }
               else
               {
                  enabledPolicyClasses.add(clazz);
               }
            }
            catch (ResourceLoadingException e)
            {
               throw new DeploymentException(CANNOT_LOAD_CLASS, className, element.getFile());
            }
         }
      }
   }
   
   private static String processNode(Node node)
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
   
   private static List<Class<?>> processElement(ResourceLoader resourceLoader, XmlElement element)
   {
      List<Class<?>> list = new ArrayList<Class<?>>();
      for (Node child : new NodeListIterable(element.getElement().getChildNodes()))
      {
         String className = processNode(child);
         if (className != null)
         {
            try
            {
               list.add(resourceLoader.classForName(className));
            }
            catch (ResourceLoadingException e)
            {
               throw new DeploymentException(CANNOT_LOAD_CLASS, className, element.getFile());
            }
         }
      }
      return list;
   }


   //TODO - move validation to Validator
   private static List<Class<?>> processInterceptorElement(ResourceLoader resourceLoader, XmlElement element)
   {
      List<Class<?>> list = new ArrayList<Class<?>>();
      for (Node child : new NodeListIterable(element.getElement().getChildNodes()))
      {
         String className = processNode(child);
         if (className != null)
         {
            try
            {
               Class<?> clazz = resourceLoader.classForName(className);
               list.add(clazz);
            }
            catch (ResourceLoadingException e)
            {
               throw new DeploymentException(CANNOT_LOAD_CLASS, className, element.getFile());
            }
         }
      }
      return list;
   }
   
}
