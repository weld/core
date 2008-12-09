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

package org.jboss.webbeans.bean;


import javax.webbeans.Instance;

import org.jboss.webbeans.InstanceImpl;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;

/**
 * Helper bean for accessing instances
 * 
 * @author Gavin King
 *
 * @param <T>
 * @param <S>
 */
public class InstanceBean<T, S> extends FacadeBean<Instance<T>, S, T>
{

   /**
    * Constructor
    * 
    * @param field The underlying fields
    * @param manager The Web Beans manager
    */
   public InstanceBean(AnnotatedItem<Instance<T>, S> field, ManagerImpl manager)
   {
      super(field, manager);
   }

   /**
    * Creates the implementing bean
    * 
    * @return The implementation
    */
   @Override
   public Instance<T> create()
   {
      return new InstanceImpl<T>(getTypeParameter(), manager, getBindingTypesArray());
   }

}
