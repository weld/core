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

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;
import org.jboss.webbeans.xml.registrator.bean.impl.BeanElementRegistratorImpl;

public abstract class NotSimpleBeanElementRegistrator extends BeanElementRegistratorImpl
{
   public NotSimpleBeanElementRegistrator(BeanChildrenChecker childrenChecker)
   {
      super(childrenChecker);
   }

   protected void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass)
   {
      // There is nothing to validate
   }
}
