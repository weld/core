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
package org.jboss.webbeans.util.collections;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of {@link ConcurrentCollection} using a
 * {@link CopyOnWriteArrayList}
 * 
 * @author Pete Muir
 * 
 */
public class ConcurrentList<E> extends CopyOnWriteArrayList<E> implements ConcurrentCollection<E>
{
   
   @SuppressWarnings("unchecked")
   private static final ConcurrentList EMPTY_LIST = new ConcurrentList();
   
   @SuppressWarnings("unchecked")
   public static <E> ConcurrentList<E> emptyList()
   {
      return EMPTY_LIST;
   }
   
   private static final long serialVersionUID = -7489797765014324457L;

   public ConcurrentList()
   {
      super();
   }

   public ConcurrentList(Collection<? extends E> collection)
   {
      super(collection);
   }

   public ConcurrentList(E[] array)
   {
      super(array);
   }
   
   
   
}
