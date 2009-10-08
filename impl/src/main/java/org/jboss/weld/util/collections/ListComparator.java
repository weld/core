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
package org.jboss.weld.util.collections;

import java.util.Comparator;
import java.util.List;

/**
 * List comparator based on element location
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class ListComparator<T> implements Comparator<T>
{
   // The source list
   private List<T> list;

   /**
    * Constructor
    * 
    * @param list The source list
    */
   public ListComparator(List<T> list)
   {
      this.list = list;
   }

   /**
    * Compares the entries
    */
   public int compare(T o1, T o2)
   {
      int p1 = list.indexOf(o1);
      int p2 = list.indexOf(o2);
      return p1 - p2;
   }

}
