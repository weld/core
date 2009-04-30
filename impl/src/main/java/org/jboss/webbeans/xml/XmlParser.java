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
package org.jboss.webbeans.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Named;
import javax.context.ScopeType;
import javax.inject.DefinitionException;
import javax.inject.DeploymentException;
import javax.inject.DeploymentType;
import javax.interceptor.InterceptorBindingType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.xml.checker.beanchildren.ext.NotSimpleBeanChildrenChecker;
import org.jboss.webbeans.xml.checker.beanchildren.ext.ResourceBeanChildrenChecker;
import org.jboss.webbeans.xml.checker.beanchildren.ext.SimpleBeanChildrenChecker;
import org.jboss.webbeans.xml.registrator.bean.BeanElementRegistrator;
import org.jboss.webbeans.xml.registrator.bean.ext.JmsResourceElementRegistrator;
import org.jboss.webbeans.xml.registrator.bean.ext.ResourceElementRegistrator;
import org.jboss.webbeans.xml.registrator.bean.ext.SessionBeanElementRegistrator;
import org.jboss.webbeans.xml.registrator.bean.ext.SimpleBeanElementRegistrator;

public class XmlParser
{
   private static Log log = Logging.getLog(XmlParser.class);

   private final XmlEnvironment environment;

   private List<BeanElementRegistrator> beanElementRegistrators = new ArrayList<BeanElementRegistrator>();

   private boolean haveAnyDeployElement = false;

   private Map<String, Set<String>> packagesMap = new HashMap<String, Set<String>>();

   public XmlParser(XmlEnvironment environment)
   {
      this.environment = environment;
   }

   public void parse()
   {
      for (URL url : environment.getBeansXmlUrls())
      {
         Document document = createDocument(url);
         if (document != null)
         {
            parseForArrays(document);
            parseForAnnotationTypes(document);
            parseForDeploy(document);
            parseForBeans(document);
         }
      }
   }

   private void parseForArrays(Document document)
   {
      Element root = document.getRootElement();
      checkChildrenForArray(root);
   }

   private void checkChildrenForArray(Element element)
   {
      Iterator<?> childIterator = element.elementIterator();
      while (childIterator.hasNext())
      {
         Element child = (Element) childIterator.next();

         if (XmlConstants.ARRAY.equalsIgnoreCase(child.getName()))
         {
            boolean haveNotAnnotation = false;
            Iterator<?> arrayIterator = child.elementIterator();
            while (arrayIterator.hasNext())
            {
               Element arrayChild = (Element) arrayIterator.next();
               AnnotatedClass<?> arrayChildType = ParseXmlHelper.loadElementClass(arrayChild, Object.class, environment, packagesMap);
               boolean isAnnotation = arrayChildType.getRawType().isAnnotation();
               if (!isAnnotation)
               {
                  if (haveNotAnnotation)
                     throw new DefinitionException("<Array> element have second child which is not annotation, it is '" + 
                           arrayChild.getName() + "'");
                  haveNotAnnotation = true;
               }
            }
            if (!haveNotAnnotation)
               throw new DefinitionException("<Array> element must have one child elemen which is not annotation");
         }
         else
            checkChildrenForArray(child);
      }
   }

   private void parseForAnnotationTypes(Document document)
   {
      Element root = document.getRootElement();

      List<Class<? extends Annotation>> bindingTypes = new ArrayList<Class<? extends Annotation>>();
      List<Class<? extends Annotation>> stereotypes = new ArrayList<Class<? extends Annotation>>();
      List<Class<? extends Annotation>> interceptorBindingTypes = new ArrayList<Class<? extends Annotation>>();
      
      Iterator<?> elIterator = root.elementIterator();
      while (elIterator.hasNext())
      {
         Element element = (Element) elIterator.next();
         boolean isBindingType = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.BINDING_TYPE).size() > 0;
         boolean isInterceptorBindingType = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.INTERCEPTOR_BINDING_TYPE).size() > 0;
         boolean isStereotype = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.STEREOTYPE).size() > 0;

         if (isBindingType || isStereotype || isInterceptorBindingType)
         {
            Class<? extends Annotation> annotationType = ParseXmlHelper.loadAnnotationClass(element, Annotation.class, environment, packagesMap);
            if (isBindingType)
            {
               bindingTypes.add(annotationType);
            }
            if (isStereotype)
            {
               stereotypes.add(annotationType);
               checkForStereotypeChildren(element);
            }
            if (isInterceptorBindingType)
            {
               interceptorBindingTypes.add(annotationType);
               checkForInterceptorBindingTypeChildren(element);
            }
         }
      }
      ParseXmlHelper.checkForUniqueElements(bindingTypes);
      ParseXmlHelper.checkForUniqueElements(stereotypes);
      ParseXmlHelper.checkForUniqueElements(interceptorBindingTypes);
   }

   private void parseForBeans(Document document)
   {
      List<Element> beanElements = findBeans(document);
      for (Element beanElement : beanElements)
      {
         AnnotatedClass<?> beanClass = ParseXmlHelper.loadElementClass(beanElement, Object.class, environment, packagesMap);
         checkBeanElement(beanElement, beanClass);
      }
   }

   private void parseForDeploy(Document document)
   {
      Element root = document.getRootElement();

      Iterator<?> elIterator = root.elementIterator();
      while (elIterator.hasNext())
      {
         Element element = (Element) elIterator.next();
         if (ParseXmlHelper.isJavaEeNamespace(element) && XmlConstants.DEPLOY.equalsIgnoreCase(element.getName()))
            environment.getEnabledDeploymentTypes().addAll(obtainDeploymentTypes(element));
      }
   }

   private Document createDocument(URL url)
   {
      try
      {
         InputStream xmlStream;

         xmlStream = url.openStream();
         if (xmlStream.available() == 0)
         {
            return null;
         }
         
         Set<URL> schemas = new HashSet<URL>();
         
         SAXReader reader = new SAXReader();
         Document document = reader.read(xmlStream);
         fullFillPackagesMap(document, url, schemas);
         
         if (schemas.size() > 0)
            ParseXmlHelper.validateXmlWithXsd(url, schemas);
         
         return document;
      }
      catch (IOException e)
      {
         String message = "Can not open stream for " + url;
         log.debug(message, e);
         throw new DefinitionException(message, e);
      }
      catch (DocumentException e)
      {
         String message = "Error during the processing of a DOM4J document for " + url;
         log.debug(message, e);
         throw new DefinitionException(message, e);
      }
   }

   private void checkForInterceptorBindingTypeChildren(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         Class<? extends Annotation> clazz = ParseXmlHelper.loadAnnotationClass(child, Annotation.class, environment, packagesMap);
         if (!XmlConstants.INTERCEPTOR_BINDING_TYPE.equalsIgnoreCase(child.getName()) && 
               !clazz.isAnnotationPresent(InterceptorBindingType.class))
            throw new DefinitionException("Direct child <" + child.getName() + "> of interceptor binding type <" + 
                  element.getName() + "> declaration must be interceptor binding type");

      }
   }

   private void checkForStereotypeChildren(Element stereotypeElement)
   {
      Iterator<?> elIterator = stereotypeElement.elementIterator();
      while (elIterator.hasNext())
      {
         Element stereotypeChild = (Element) elIterator.next();
         Class<? extends Annotation> stereotypeClass = ParseXmlHelper.loadAnnotationClass(stereotypeChild, Annotation.class, environment, packagesMap);
         if (XmlConstants.STEREOTYPE.equalsIgnoreCase(stereotypeChild.getName()) || 
               stereotypeClass.isAnnotationPresent(ScopeType.class) || 
               stereotypeClass.isAnnotationPresent(DeploymentType.class) || 
               stereotypeClass.isAnnotationPresent(InterceptorBindingType.class) ||
               stereotypeClass.equals(Named.class))
            continue;
         throw new DefinitionException("Direct child <" + stereotypeChild.getName() + "> of stereotype <" + stereotypeElement.getName() + 
               "> declaration must be scope type, or deployment type, or interceptor binding type, or javax.annotation.Named");
      }
   }

   private List<Element> findBeans(Document document)
   {
      List<Element> beans = new ArrayList<Element>();

      Element root = document.getRootElement();

      Iterator<?> elIterator = root.elementIterator();
      while (elIterator.hasNext())
      {
         Element element = (Element) elIterator.next();
         if (checkBeanElementName(element) && checkBeanElementChildrenNames(element))
            beans.add(element);
      }

      return beans;
   }

   private boolean checkBeanElementName(Element element)
   {
      if (ParseXmlHelper.isJavaEeNamespace(element) && 
            (XmlConstants.DEPLOY.equalsIgnoreCase(element.getName()) || 
                  XmlConstants.INTERCEPTORS.equalsIgnoreCase(element.getName()) || 
                  XmlConstants.DECORATORS.equalsIgnoreCase(element.getName())))
         return false;
      return true;
   }

   private boolean checkBeanElementChildrenNames(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (ParseXmlHelper.isJavaEeNamespace(child) && 
               (XmlConstants.BINDING_TYPE.equalsIgnoreCase(child.getName()) || 
                     XmlConstants.INTERCEPTOR_BINDING_TYPE.equalsIgnoreCase(child.getName()) || 
                     XmlConstants.STEREOTYPE.equalsIgnoreCase(child.getName())))
            return false;
      }
      return true;
   }

   // TODO Make this object orientated
   private List<Class<? extends Annotation>> obtainDeploymentTypes(Element element)
   {
      if (haveAnyDeployElement)
         throw new DefinitionException("<Deploy> element is specified more than once");

      List<Element> standardElements = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.STANDARD);
      if (standardElements.size() == 0)
         throw new DeploymentException("The @Standard deployment type must be declared");

      List<Class<? extends Annotation>> deploymentClasses = new ArrayList<Class<? extends Annotation>>();
      Iterator<?> deployIterator = element.elementIterator();
      while (deployIterator.hasNext())
      {
         Element deploymentElement = (Element) deployIterator.next();

         String elementName = deploymentElement.getName();
         String elementPrefix = deploymentElement.getNamespacePrefix();
         String elementUri = deploymentElement.getNamespaceURI();
         List<Element> deploymentElements = ParseXmlHelper.findElements(element, elementName, elementPrefix, elementUri);
         if (deploymentElements.size() != 1)
            throw new DefinitionException("The same deployment type '" + deploymentElement.getName() + "' is declared more than once");

         Class<? extends Annotation> deploymentClass = ParseXmlHelper.loadAnnotationClass(deploymentElement, Annotation.class, environment, packagesMap);

         if (!deploymentClass.isAnnotationPresent(DeploymentType.class))
            throw new DefinitionException("<Deploy> child '" + deploymentElement.getName() + "' must be a deployment type");

         deploymentClasses.add(deploymentClass);
      }
      haveAnyDeployElement = true;
      return deploymentClasses;
   }

   private void checkBeanElement(Element beanElement, AnnotatedClass<?> beanClass)
   {
      beanElementRegistrators.add(new JmsResourceElementRegistrator(new NotSimpleBeanChildrenChecker(environment, packagesMap)));
      beanElementRegistrators.add(new ResourceElementRegistrator(new ResourceBeanChildrenChecker(environment, packagesMap)));
      beanElementRegistrators.add(new SessionBeanElementRegistrator(new NotSimpleBeanChildrenChecker(environment, packagesMap), environment.getEjbDescriptors()));
      beanElementRegistrators.add(new SimpleBeanElementRegistrator(new SimpleBeanChildrenChecker(environment, packagesMap), environment.getEjbDescriptors()));

      boolean isValidType = false;
      for (BeanElementRegistrator beanElementRegistrator : beanElementRegistrators)
      {
         if (beanElementRegistrator.accept(beanElement, beanClass))
         {
            beanElementRegistrator.registerBeanElement(beanElement, beanClass);
            isValidType = true;
            break;
         }
      }

      if (!isValidType)
         throw new DefinitionException("Can't determine type of bean element <" + beanElement.getName() + ">");
   }

   private void fullFillPackagesMap(Document document, URL xmlUrl, Set<URL> schemas)
   {
      Element root = document.getRootElement();
      ParseXmlHelper.checkRootAttributes(root, packagesMap, environment, xmlUrl, schemas);
      ParseXmlHelper.checkRootDeclaredNamespaces(root, packagesMap, environment, xmlUrl, schemas);
   }
}
