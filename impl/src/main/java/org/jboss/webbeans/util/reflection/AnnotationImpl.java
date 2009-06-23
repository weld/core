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
package org.jboss.webbeans.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Supports instantiation of annotation type instances.
 * 
 * @author Pete Muir
 * @author Gavin King
 * 
 *  @param <T>
 *            the annotation type
 */
public class AnnotationImpl<T extends Annotation> implements Annotation
{
   
   private final Class<T> annotationType;
   private final Method[] members;

   public AnnotationImpl(Class<T> annotationType, Method[] members)
   {
      this.annotationType = annotationType;
      this.members = members;
   }

   public Class<? extends Annotation> annotationType()
   {
      return annotationType;
   }

   @Override
   public String toString()
   {
     String string = "@" + annotationType().getName() + "(";
     for (int i = 0; i < members.length; i++)
     {
        string += members[i].getName() + "=";
        string += invoke(members[i], this);
        if (i < members.length - 1)
        {
           string += ",";
        }
     }
     return string + ")";
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof Annotation)
      {
         Annotation that = (Annotation) other;
         if (this.annotationType().equals(that.annotationType()))
         {
            for (Method member : members)
            {
               Object thisValue = invoke(member, this);
               Object thatValue = invoke(member, that);
               if (!thisValue.equals(thatValue))
               {
                  return false;
               }
            }
            return true;
         }
      }
      return false;
   }
   
   @Override
   public int hashCode()
   {
      int hashCode = 0;
      for (Method member : members)
      {
         int memberNameHashCode = 127 * member.getName().hashCode();
         int memberValueHashCode = invoke(member, this).hashCode();
         hashCode += memberNameHashCode ^ memberValueHashCode;
      }       
      return hashCode;
   }
   
   private static Object invoke(Method method, Object instance)
   {
      try
      {
         method.setAccessible(true);
         return method.invoke(instance);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
   }
}
