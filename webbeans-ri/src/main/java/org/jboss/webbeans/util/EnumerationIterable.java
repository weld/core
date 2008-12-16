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

package org.jboss.webbeans.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An Enumeration -> Iteratble adaptor
 *  
 * @author Pete Muir
 * @see org.jboss.webbeans.util.EnumerationIterator
 */
public class EnumerationIterable<T> implements Iterable<T>
{
   // The enumeration-iteartor
   private EnumerationIterator<T> iterator;
   
   /**
    * Constructor
    * 
    * @param enumeration The enumeration
    */
   public EnumerationIterable(Enumeration<T> enumeration)
   {
      this.iterator = new EnumerationIterator<T>(enumeration);
   }
   
   /**
    * Gets an iterator
    * 
    * @return The iterator
    */
   public Iterator<T> iterator()
   {
      return iterator;
   }
   
}
