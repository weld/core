/*
 * JBoss, Home of Professional Open Source
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
e @authors tag. See the copyright.txt in the distribution for a
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
package org.jboss.webbeans.util.collections;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adds concurrent add operations to the collection interface. Signatures and
 * javadoc taken from {@link CopyOnWriteArrayList}.
 * 
 * @author Pete Muir
 *
 */
public interface ConcurrentCollection<E> extends Collection<E>
{
   /**
    * Appends all of the elements in the specified collection that are not
    * already contained in this list, to the end of this list, in the order that
    * they are returned by the specified collection's iterator.
    * 
    * @param c
    *           collection containing elements to be added to this list
    * @return the number of elements added
    * @throws NullPointerException
    *            if the specified collection is null
    */
   public int addAllAbsent(Collection<? extends E> c);
   
   /**
    * Append the element if not present.
    * 
    * @param e
    *           element to be added to this list, if absent
    * @return true if the element was added
    */
   public boolean addIfAbsent(E e);
   
}
