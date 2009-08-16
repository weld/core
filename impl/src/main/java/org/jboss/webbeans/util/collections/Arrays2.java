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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pmuir
 *
 */
public class Arrays2
{
   
   private Arrays2() {}
   
   public static final boolean containsAll(Object[] array, Object... values)
   {
      return Arrays.asList(array).containsAll(Arrays.asList(values));
   }
   
   public static final boolean unorderedEquals(Object[] array, Object... values)
   {
      return containsAll(array, values) && array.length == values.length;
   }

   public static <T> Set<T> asSet(T... types)
   {
      Set<T> result = new HashSet<T>();
      for (T type : types)
      {
         result.add(type);
      }
      return result;
   }

}
