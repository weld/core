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

import javax.decorator.Decorator;
import javax.inject.DefinitionException;
import javax.interceptor.Interceptor;

import org.dom4j.Element;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;
import org.jboss.webbeans.xml.registrator.bean.impl.BeanElementRegistratorImpl;

public class SimpleBeanElementRegistrator extends BeanElementRegistratorImpl
{
   private final EjbDescriptorCache ejbDescriptors;

   public SimpleBeanElementRegistrator(BeanChildrenChecker childrenChecker, EjbDescriptorCache ejbDescriptors)
   {
      super(childrenChecker);
      this.ejbDescriptors = ejbDescriptors;
   }

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      boolean isSessionBean = ejbDescriptors.containsKey(beanElement.getName()) || beanElement.attribute(XmlConstants.EJB_NAME) != null;

      if (!beanClass.isAbstract() && !isSessionBean && !beanClass.isParameterizedType())
      {
         return true;
      }

      return false;
   }

   protected void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if (beanClass.isNonStaticMemberClass())
         throw new DefinitionException("Bean class '" + beanClass.getName() + "' of a simple bean <" + 
               beanElement.getName() + "> is a non static member class");

      if (beanClass.getRawType().isAnnotationPresent(Interceptor.class) && 
            ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() != 1)
         throw new DefinitionException("A simple bean defined in XML as <" + beanElement.getName() + "> has a bean class '" + 
               beanClass.getName() + "' annotated @Interceptor and is not declared as an interceptor in XML");

      if (beanClass.getRawType().isAnnotationPresent(Decorator.class) && 
            ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() != 1)
         throw new DefinitionException("A simple bean defined in XML as <" + beanElement.getName() + "> has a bean class '" + 
               beanClass.getName() + "' annotated @Decorator and is not declared as an decorator in XML");
   }
}
