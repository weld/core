/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Supplier;

public class HashSetSupplier<T> implements Supplier<Set<T>>
{
   
   private static final Supplier<?> INSTANCE = new HashSetSupplier<Object>();
   
   @SuppressWarnings("unchecked")
   public static <T> Supplier<Set<T>> instance()
   {
      return (Supplier<Set<T>>) INSTANCE;
   }
   
   private HashSetSupplier() {}
   
   public Set<T> get()
   {
      return new HashSet<T>();
   }

}
