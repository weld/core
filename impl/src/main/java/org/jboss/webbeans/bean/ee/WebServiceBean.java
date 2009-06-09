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
package org.jboss.webbeans.bean.ee;

import java.lang.annotation.Annotation;
import java.util.Set;

import javassist.util.proxy.MethodHandler;

import org.jboss.webbeans.BeanManagerImpl;

/**
 * @author Pete Muir
 *
 */
public class WebServiceBean<T> extends AbstractResourceBean<T>
{
   
   private final String id;
   private final String wsdlLocation;

   public WebServiceBean(BeanManagerImpl manager, Class<? extends Annotation> deploymentType, Set<Annotation> bindings, Class<T> type, String jndiName, String mappedName, String wsdlLocation)
   {
      super(manager, deploymentType, bindings, type, jndiName, mappedName, type);
      this.wsdlLocation = wsdlLocation;
      this.id = createId("WebService - " );
   }

   @Override
   public String getId()
   {
      return id;
   }
   
   public String getWsdlLocation()
   {
      return wsdlLocation;
   }
   
   @Override
   protected MethodHandler newMethodHandler()
   {
      return new WebServiceMethodHandler(getJndiName(), getMappedName());
   }
   
}
