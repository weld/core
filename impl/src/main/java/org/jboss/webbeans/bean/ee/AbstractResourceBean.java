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
import java.lang.reflect.Type;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;

/**
 * @author  Pete Muir
 */
public abstract class AbstractResourceBean<T> extends AbstractJavaEEResourceBean<T>
{
   
   private final String jndiName;
   private final String mappedName;

   protected AbstractResourceBean(ManagerImpl manager, Class<? extends Annotation> deploymentType, Set<Annotation> bindings, Class<T> type, String jndiName, String mappedName, Type... types)
   {
      super(manager, deploymentType, bindings, type, types);
      this.jndiName = jndiName;
      this.mappedName = mappedName;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public String getMappedName()
   {
      return mappedName;
   }
   
}