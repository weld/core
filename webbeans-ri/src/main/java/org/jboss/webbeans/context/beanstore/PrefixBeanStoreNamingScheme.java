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
package org.jboss.webbeans.context.beanstore;

import javax.context.Contextual;

import org.jboss.webbeans.CurrentManager;

/**
 * Simple prefix-based implementation of a bean map naming scheme
 * 
 * @author Nicklas Karlsson
 */
public class PrefixBeanStoreNamingScheme implements BeanStoreNamingScheme
{
   public String prefix;
   public String delimeter;

   public PrefixBeanStoreNamingScheme(String prefix, String delimeter)
   {
      if (prefix.indexOf(delimeter) >= 0)
      {
         throw new IllegalArgumentException("The prefix '" + prefix + "' shouldn't be in the prefix '" + prefix + "'");
      }
      this.prefix = prefix;
      this.delimeter = delimeter;
   }

   public boolean acceptKey(String key)
   {
      return key.startsWith(prefix);
   }

   public int getBeanIndexFromKey(String key)
   {
      return Integer.parseInt(key.substring(prefix.length() + delimeter.length()));
   }

   public String getContextualKey(Contextual<?> contextual)
   {
      return prefix + delimeter + CurrentManager.rootManager().getBeans().indexOf(contextual);
   }

}
