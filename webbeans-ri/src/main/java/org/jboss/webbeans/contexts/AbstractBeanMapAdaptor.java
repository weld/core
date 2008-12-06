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

package org.jboss.webbeans.contexts;

import javax.webbeans.manager.Contextual;

import org.jboss.webbeans.CurrentManager;

public abstract class AbstractBeanMapAdaptor implements BeanMap
{
   
   /**
    * Gets a key prefix that should be prefixed to names
    * 
    * @return The prefix
    */
   protected abstract String getKeyPrefix();
   
   /**
    * Returns a map key to a bean. Uses a known prefix and appends the index of
    * the Bean in the Manager bean list.
    * 
    * @param bean The bean to generate a key for.
    * @return A unique key;
    */
   protected String getBeanKey(Contextual<?> bean)
   {
      return getKeyPrefix() + "#" + CurrentManager.rootManager().getBeans().indexOf(bean);
   }
   
}
