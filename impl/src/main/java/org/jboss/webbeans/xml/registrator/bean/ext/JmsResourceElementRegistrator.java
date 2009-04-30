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
import java.util.List;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.ee.AbstractJavaEEResourceBean;
import org.jboss.webbeans.bean.ee.jms.JmsQueueBean;
import org.jboss.webbeans.bean.ee.jms.JmsTopicBean;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.messaging.spi.JmsServices;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public class JmsResourceElementRegistrator extends ResourceElementRegistrator
{
   public JmsResourceElementRegistrator(BeanChildrenChecker childrenChecker)
   {
      super(childrenChecker);
   }

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if (ParseXmlHelper.isJavaEeNamespace(beanElement) && 
            (XmlConstants.TOPIC.equalsIgnoreCase(beanElement.getName()) || 
                  XmlConstants.QUEUE.equalsIgnoreCase(beanElement.getName())))
         return true;
      return false;
   }
   
   protected void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass)
   {
      List<Element> resourceElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.RESOURCE);
      if (resourceElements.size() == 0)
         throw new DefinitionException("Each JMS resource declaration must contain a child <Resource> element, " +
               "but there is noone in <" + beanElement.getName() + ">");
   }
   
   protected void register(Element beanElement, AnnotatedClass<?> beanClass)
   {
      Element resourceElement = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.RESOURCE).get(0);
      
      Class<? extends Annotation> deploymentType = obtainDeploymentType(beanElement);
      Set<Annotation> bindings = obtainBindings(beanElement);
      String jndiName = obtainElementValue(resourceElement, XmlConstants.JNDI_NAME);
      String mappedName = obtainElementValue(resourceElement, XmlConstants.MAPPED_NAME);

      RIBean<?> bean = null;
      
      if (XmlConstants.TOPIC.equalsIgnoreCase(beanElement.getName()))
         bean = new JmsTopicBean(environment.getManager(), deploymentType, bindings, jndiName, mappedName);
      else
         bean = new JmsQueueBean(environment.getManager(), deploymentType, bindings, jndiName, mappedName);
      
      if (environment.getServices().contains(JmsServices.class))
      {
         environment.getResourceBeans().add((AbstractJavaEEResourceBean<?>) bean);
      }
   }
}
