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
package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.util.Map;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;

public class NotSimpleBeanChildrenChecker extends AbstractBeanChildrenChecker
{
   public NotSimpleBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }

   protected void checkForInterceptorChild(Element beanElement)
   {
      if (ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() > 1)
         throw new DefinitionException("Not a simple bean '" + beanElement.getName() + "' contains direct child <" + 
               XmlConstants.INTERCEPTOR + ">");
   }

   protected void checkForDecoratorChild(Element beanElement)
   {
      if (ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() > 1)
         throw new DefinitionException("Not a simple bean '" + beanElement.getName() + "' contains direct child <" + 
               XmlConstants.DECORATOR + ">");
   }

   protected void checkChildForInterceptorType(Element beanChildElement)
   {
      throw new DefinitionException("Declaration of not a simple bean '" + beanChildElement.getParent().getName() + 
            "' contains a child <" + beanChildElement.getName() + "> which type is javax.interceptor.Interceptor");
   }

   protected void checkChildForDecoratorType(Element beanChildElement)
   {
      throw new DefinitionException("Declaration of not a simple bean '" + beanChildElement.getParent().getName() + 
            "' contains a child <" + beanChildElement.getName() + "> which type is javax.decorator.Decorator");
   }

   protected void checkForConstructor(Element beanElement, AnnotatedClass<?> beanClass)
   {
      // There is nothing to validate
   }

   protected void checkRIBean(Element beanElement, AnnotatedClass<?> beanClass)
   {
      throw new DefinitionException("It is impossible determine some kind of resource in not Resource Bean");
   }
}
