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

package org.jboss.webbeans.injection;

import java.util.Stack;

import javax.inject.manager.InjectionPoint;

/**
 * Provides injection point metadata
 * 
 * @author David Allen
 * @author Nicklas Karlsson
 */
public class InjectionPointProvider
{
   // The stack of injection points
   private final Stack<InjectionPoint> injectionPoints = new Stack<InjectionPoint>();

   /**
    * Pushes an injection point to the stack
    * 
    * @param injectionPoint The injection point to push
    */
   public void pushInjectionPoint(AnnotatedInjectionPoint<?, ?> injectionPoint)
   {
      injectionPoints.push(injectionPoint);
   }

   /**
    * Pops an injection point
    */
   public void popInjectionPoint()
   {
      if (injectionPoints.isEmpty())
      {
         return;
      }
      injectionPoints.pop();
   }

   /**
    * Gets the current injection point
    * 
    * @return The current injection point or null if there is none on the stack
    */
   public InjectionPoint getCurrentInjectionPoint()
   {
      return injectionPoints.isEmpty() ? null : injectionPoints.peek();
   }

   /**
    * Gets the previous injection point
    * 
    * @return The previous injection point or null if there is none on the stack
    */
   public InjectionPoint getPreviousInjectionPoint()
   {
      return injectionPoints.size() < 2 ? null : injectionPoints.elementAt(injectionPoints.size() - 2);
   }

   @Override
   public String toString()
   {
      return "InjectionPoint stack = " + injectionPoints.toString();
   }

}
