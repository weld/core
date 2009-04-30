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
package org.jboss.webbeans.xml.registrator.bean.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Current;
import javax.inject.DeploymentType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.dom4j.Element;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.ee.AbstractJavaEEResourceBean;
import org.jboss.webbeans.bean.ee.PersistenceContextBean;
import org.jboss.webbeans.bean.ee.PersistenceUnitBean;
import org.jboss.webbeans.bean.ee.RemoteEjbBean;
import org.jboss.webbeans.bean.ee.ResourceBean;
import org.jboss.webbeans.bean.ee.WebServiceBean;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.util.reflection.AnnotationImpl;
import org.jboss.webbeans.ws.spi.WebServices;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public class ResourceElementRegistrator extends NotSimpleBeanElementRegistrator
{
   public ResourceElementRegistrator(BeanChildrenChecker childrenChecker)
   {
      super(childrenChecker);
   }

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if (ParseXmlHelper.isJavaEeNamespace(beanElement) && 
            (XmlConstants.TOPIC.equalsIgnoreCase(beanElement.getName()) || 
                  XmlConstants.QUEUE.equalsIgnoreCase(beanElement.getName())))
         return false;

      Iterator<?> elIterator = beanElement.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (ParseXmlHelper.isJavaEeNamespace(child) && 
               (XmlConstants.RESOURCE.equalsIgnoreCase(child.getName()) || 
                     XmlConstants.PERSISTENCE_CONTEXT.equalsIgnoreCase(child.getName()) || 
                     XmlConstants.PERSISTENCE_UNIT.equalsIgnoreCase(child.getName()) || 
                     XmlConstants.EJB.equalsIgnoreCase(child.getName()) || 
                     XmlConstants.WEB_SERVICE_REF.equalsIgnoreCase(child.getName())))
            return true;
      }
      return false;
   }

   protected void register(Element beanElement, AnnotatedClass<?> beanClass)
   {
      List<Element> resourceElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.RESOURCE);
      if (resourceElements.size() > 0)
      {
         registerResourceBean(resourceElements.get(0), beanClass);
         return;
      }

      List<Element> persContextElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.PERSISTENCE_CONTEXT);
      if (persContextElements.size() > 0)
      {
         registerPersContextBean(persContextElements.get(0), beanClass);
         return;
      }

      List<Element> persUnitElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.PERSISTENCE_UNIT);
      if (persUnitElements.size() > 0)
      {
         registerPersUnitBean(persUnitElements.get(0), beanClass);
         return;
      }

      List<Element> ejbElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.EJB);
      if (ejbElements.size() > 0)
      {
         registerEjbBean(ejbElements.get(0), beanClass);
         return;
      }

      List<Element> webServiceRefElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.WEB_SERVICE_REF);
      if (webServiceRefElements.size() > 0)
      {
         registerWebServiceRefBean(webServiceRefElements.get(0), beanClass);
         return;
      }
   }

   private void registerResourceBean(Element resourceElement, AnnotatedClass<?> beanClass)
   {
      Class<? extends Annotation> deploymentType = obtainDeploymentType(resourceElement.getParent());
      Set<Annotation> bindings = obtainBindings(resourceElement.getParent());
      Class<?> type = beanClass.getRawType();
      String jndiName = obtainElementValue(resourceElement, XmlConstants.JNDI_NAME);
      String mappedName = obtainElementValue(resourceElement, XmlConstants.MAPPED_NAME);

      RIBean<?> bean = new ResourceBean(environment.getManager(), deploymentType, bindings, type, jndiName, mappedName);

      if (environment.getServices().contains(ResourceServices.class))
      {
         environment.getResourceBeans().add((AbstractJavaEEResourceBean<?>) bean);
      }
   }

   private void registerPersContextBean(Element persContextElement, AnnotatedClass<?> beanClass)
   {
      Class<? extends Annotation> deploymentType = obtainDeploymentType(persContextElement.getParent());
      Set<Annotation> bindings = obtainBindings(persContextElement.getParent());
      String unitName = obtainElementValue(persContextElement, XmlConstants.UNIT_NAME);

      RIBean<?> bean = new PersistenceContextBean(environment.getManager(), deploymentType, bindings, unitName);

      if (environment.getServices().contains(JpaServices.class))
      {
         environment.getResourceBeans().add((AbstractJavaEEResourceBean<?>) bean);
      }
   }

   private void registerPersUnitBean(Element persUnitElement, AnnotatedClass<?> beanClass)
   {
      Class<? extends Annotation> deploymentType = obtainDeploymentType(persUnitElement.getParent());
      Set<Annotation> bindings = obtainBindings(persUnitElement.getParent());
      String unitName = obtainElementValue(persUnitElement, XmlConstants.UNIT_NAME);

      RIBean<?> bean = new PersistenceUnitBean(environment.getManager(), deploymentType, bindings, unitName);

      if (environment.getServices().contains(JpaServices.class))
      {
         environment.getResourceBeans().add((AbstractJavaEEResourceBean<?>) bean);
      }
   }

   private void registerEjbBean(Element ejbElement, AnnotatedClass<?> beanClass)
   {
      Class<? extends Annotation> deploymentType = obtainDeploymentType(ejbElement.getParent());
      Set<Annotation> bindings = obtainBindings(ejbElement.getParent());
      Class<?> type = beanClass.getRawType();
      String jndiName = obtainElementValue(ejbElement, XmlConstants.JNDI_NAME);
      String mappedName = obtainElementValue(ejbElement, XmlConstants.MAPPED_NAME);
      String ejbLink = obtainElementValue(ejbElement, XmlConstants.EJB_LINK);
      ;

      RIBean<?> bean = new RemoteEjbBean(environment.getManager(), deploymentType, bindings, type, jndiName, mappedName, ejbLink);

      if (environment.getServices().contains(EjbServices.class))
      {
         environment.getResourceBeans().add((AbstractJavaEEResourceBean<?>) bean);
      }
   }

   private void registerWebServiceRefBean(Element webServiceRefElement, AnnotatedClass<?> beanClass)
   {
      Class<? extends Annotation> deploymentType = obtainDeploymentType(webServiceRefElement.getParent());
      Set<Annotation> bindings = obtainBindings(webServiceRefElement.getParent());
      Class<?> type = beanClass.getRawType();
      String jndiName = obtainElementValue(webServiceRefElement, XmlConstants.JNDI_NAME);
      String mappedName = obtainElementValue(webServiceRefElement, XmlConstants.MAPPED_NAME);
      String wsdlLocation = obtainElementValue(webServiceRefElement, XmlConstants.WSDL_LOCATION);

      RIBean<?> bean = new WebServiceBean(environment.getManager(), deploymentType, bindings, type, jndiName, mappedName, wsdlLocation);

      if (environment.getServices().contains(WebServices.class))
      {
         environment.getResourceBeans().add((AbstractJavaEEResourceBean<?>) bean);
      }
   }

   protected Class<? extends Annotation> obtainDeploymentType(Element beanElement)
   {
      Iterator<?> elIterator = beanElement.elementIterator();
      while (elIterator.hasNext())
      {
         Element childElement = (Element) elIterator.next();
         AnnotatedClass<?> childClass = ParseXmlHelper.loadElementClass(childElement, Object.class, environment, packagesMap);
         if (childClass.getRawType().isAnnotation() && childClass.isAnnotationPresent(DeploymentType.class))
            return ParseXmlHelper.loadAnnotationClass(childElement, Annotation.class, environment, packagesMap);
      }

      return null;
   }

   protected Set<Annotation> obtainBindings(Element beanElement)
   {
      Set<Annotation> result = new HashSet<Annotation>();

      Iterator<?> elIterator = beanElement.elementIterator();
      while (elIterator.hasNext())
      {
         Element childElement = (Element) elIterator.next();
         AnnotatedClass<?> childClass = ParseXmlHelper.loadElementClass(childElement, Object.class, environment, packagesMap);
         if (childClass.getRawType().isAnnotation() && !childClass.isAnnotationPresent(DeploymentType.class) && !childClass.getRawType().equals(Resource.class) && !childClass.getRawType().equals(PersistenceContext.class) && !childClass.getRawType().equals(PersistenceUnit.class) && !childClass.getRawType().equals(EJB.class) && !childClass.getRawType().equals(WebServiceRef.class))
         {
            Class<?> annotationClass = childClass.getRawType();
            Method[] annotationMethods = annotationClass.getDeclaredMethods();
            AnnotationImpl annotation = new AnnotationImpl(annotationClass, annotationMethods);
            result.add(annotation);
         }
      }

      if (result.size() == 0)
      {
         Class<?> annotationClass = Current.class;
         Method[] annotationMethods = annotationClass.getDeclaredMethods();
         AnnotationImpl annotation = new AnnotationImpl(annotationClass, annotationMethods);
         result.add(annotation);
      }

      return result;
   }

   protected String obtainElementValue(Element elementParent, String elementName)
   {
      List<Element> elements = ParseXmlHelper.findElementsInEeNamespace(elementParent, elementName);
      if (elements.size() > 0)
      {
         Element element = elements.get(0);
         return element.getData().toString();
      }
      return null;
   }
}
