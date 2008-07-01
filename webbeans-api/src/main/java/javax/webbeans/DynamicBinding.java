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

package javax.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 
 * @author Pete Muir
 */

public abstract class DynamicBinding<T extends Annotation> implements Annotation
{

   private Class<T> annotationType;
   
   private static <T> Class<T> getAnnotationType(Class<?> clazz)
   {
      
      Type type = clazz.getGenericSuperclass();
      Class<T> annotationType = null;
      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         if (parameterizedType.getActualTypeArguments().length == 1)
         {
            annotationType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
         }
      }
      if (annotationType == null && clazz != Object.class)
      {
         return getAnnotationType(clazz.getSuperclass());
      }
      else
      {
         return annotationType;
      }
   }
   
   public Class<? extends Annotation> annotationType()
   {
      annotationType = getAnnotationType(getClass());
      if (annotationType == null)
      {
         throw new RuntimeException("Unable to determine type of dynamic binding for " + getClass());
      }
      return annotationType;
   }
   
   @Override
   public String toString()
   {
      // TODO Make this closer to the spec for Annotation
      String annotationName = "@" + annotationType().getName();
      return annotationName;
   }
   
}
