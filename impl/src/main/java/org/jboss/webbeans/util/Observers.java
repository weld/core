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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.enterprise.event.Observer;

/**
 * @author pmuir
 *
 */
public class Observers
{

   public static Type getTypeOfObserver(Observer<?> observer)
   {
      for (Type type : observer.getClass().getGenericInterfaces())
      {
         if (type instanceof ParameterizedType)
         {
            ParameterizedType ptype = (ParameterizedType) type;
            if (Observer.class.isAssignableFrom((Class<?>) ptype.getRawType()))
            {
               return ptype.getActualTypeArguments()[0];
            }
         }
      }
      throw new RuntimeException("Cannot find observer's event type: " + observer);
   }
   
}
