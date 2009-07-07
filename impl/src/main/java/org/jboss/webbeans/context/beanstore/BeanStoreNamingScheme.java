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


/**
 * Interface against a BeanStore to handle different naming schemes
 * 
 * @author Nicklas Karlsson
 *
 */
public interface BeanStoreNamingScheme
{
   /**
    * Checks if a key is handled by the bean store
    * 
    * @param key The key to match
    * @return True if match, false otherwise
    */
   public abstract boolean acceptKey(String key);
   
   /**
    * Gets a bean store key for a contextual
    * 
    * @param contextual The contextual to make the key for
    * @return A map key
    */
   public abstract String getKeyFromId(Integer id);
   
   /**
    * Gets a contextual id from a key
    * 
    * @param key The key to parse
    * @return The contextual id
    */
   public abstract Integer getIdFromKey(String key);
}
