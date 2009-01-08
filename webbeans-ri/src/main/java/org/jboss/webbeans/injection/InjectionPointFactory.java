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

import java.lang.reflect.Member;
import java.util.Stack;

import javax.webbeans.InjectionPoint;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.introspector.AnnotatedMember;

/**
 * Factory used to create the container provided implementation for the
 * InjectionPoint beans. This factory maintains a stack with the current bean
 * and instance being created so that this information is readily available for
 * construction of a new InjectionPoint bean.
 * 
 * @author David Allen
 * 
 */
public class InjectionPointFactory
{
   private final Stack<Bean<?>> beans = new Stack<Bean<?>>();
   private final Stack<Object> beanInstances = new Stack<Object>();
   private final Stack<AnnotatedMember<?, ? extends Member>> injectionPoints = new Stack<AnnotatedMember<?, ? extends Member>>();

   /**
    * Pushes the current bean that is being instantiated onto a stack for later
    * use.
    * 
    * @param currentBean The bean being instantiated
    */
   public void pushBean(Bean<?> currentBean)
   {
      beans.push(currentBean);
   }

   /**
    * Pushes the current bean instance that has been instantiated, but has not
    * yet had any injection points initialized.
    * 
    * @param currentInstance The bean instance last instantiated
    */
   public void pushInstance(Object currentInstance)
   {
      beanInstances.push(currentInstance);
   }

   /**
    * Pushes the current injection point being processed.
    * 
    * @param injectedMember The metadata for the injection point
    */
   public void pushInjectionPoint(AnnotatedMember<?, ? extends Member> injectedMember)
   {
      injectionPoints.push(injectedMember);
   }

   /**
    * Pops the bean from the stack.  This should be called
    * whenever all processing is complete for instantiating a bean.
    */
   public void popBean()
   {
      beans.pop();
   }

   /**
    * Pops the current instance from the stack.  This should be called
    * whenever all processing is complete for instantiating a bean.
    */
   public void popInstance()
   {
      beanInstances.pop();
   }

   /**
    * Pops the current injection point being processed.  This should be called once
    * the injection point is bound.
    */
   public void popInjectionPoint()
   {
      injectionPoints.pop();
   }

   /**
    * Creates an InjectionPoint based on the current state of processing as
    * indicated by this factory's stack of injection points and related
    * information.
    * 
    * @return a new injection point metadata object
    */
   public InjectionPoint getPreviousInjectionPoint()
   {
      // When the injected member is a constructor, we are short one instance,
      // so the instance on the top of the stack is the bean instance
      // we want. Otherwise, it is the second to last instance same as
      // the bean stack.
      InjectionPoint injectionPoint = null;
      Bean<?> currentBean = beans.pop();
      AnnotatedMember<?, ? extends Member> currentInjection = injectionPoints.pop();
      if (beanInstances.size() < beans.size())
      {
         injectionPoint = new InjectionPointImpl(injectionPoints.peek(), beans.peek(), beanInstances.peek());
      }
      else
      {
         Object currentInstance = beanInstances.pop();
         injectionPoint = new InjectionPointImpl(injectionPoints.peek(), beans.peek(), beanInstances.peek());
         beanInstances.push(currentInstance);
      }
      beans.push(currentBean);
      injectionPoints.push(currentInjection);
      return injectionPoint;
   }
}
