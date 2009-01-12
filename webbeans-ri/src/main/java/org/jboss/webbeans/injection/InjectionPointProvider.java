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
 * Used to create the container provided implementation for the InjectionPoint
 * beans. The instance maintains state information on a stack so that this
 * information is readily available for construction of a new InjectionPoint
 * bean instance.
 * 
 * @author David Allen
 * 
 */
public class InjectionPointProvider
{
   private final Stack<Bean<?>> beans = new Stack<Bean<?>>();
   private final Stack<AnnotatedMember<?, ? extends Member>> injectionPoints = new Stack<AnnotatedMember<?, ? extends Member>>();

   /**
    * Pushes the current bean that is being instantiated onto a stack for later
    * use. This always pushes a null bean instance on the stack too which can
    * later be replaced by a real instance.
    * 
    * @param currentBean The bean being instantiated
    */
   public void pushBean(Bean<?> currentBean)
   {
      beans.push(currentBean);
   }

   /**
    * Pushes the current injection point member being processed.
    * 
    * @param injectedMember The metadata for the injection point member
    */
   public void pushInjectionMember(AnnotatedMember<?, ? extends Member> injectedMember)
   {
      injectionPoints.push(injectedMember);
   }

   /**
    * Pops the bean from the stack. This should be called whenever all
    * processing is complete for instantiating a bean.
    */
   public void popBean()
   {
      beans.pop();
   }

   /**
    * Pops the current injection point being processed. This should be called
    * once the injection point is bound.
    */
   public void popInjectionMember()
   {
      injectionPoints.pop();
   }

   /**
    * Returns the InjectionPoint where the current bean under construction is
    * being injected.
    * 
    * @return a new injection point metadata object
    */
   public InjectionPoint getPreviousInjectionPoint()
   {
      return new InjectionPointImpl(getPreviousInjectionMember(), getPreviousBean());
   }

   /**
    * Returns the injection point metadata for the injection point currently
    * being injected.
    * 
    * @return current injection point metadata object
    */
   public InjectionPoint getCurrentInjectionPoint()
   {
      return new InjectionPointImpl(getCurrentInjectionMember(), getCurrentBean());
   }

   protected Bean<?> getCurrentBean()
   {
      return beans.peek();
   }
   protected AnnotatedMember<?, ? extends Member> getCurrentInjectionMember()
   {
      if (injectionPoints.size() > 0)
         return injectionPoints.peek();
      else
         return null;
   }

   protected Bean<?> getPreviousBean()
   {
      Bean<?> currentBean = beans.pop();
      Bean<?> result = beans.peek();
      beans.push(currentBean);
      return result;
   }

   protected AnnotatedMember<?, ? extends Member> getPreviousInjectionMember()
   {
      AnnotatedMember<?, ? extends Member> result = null;
      if (injectionPoints.size() < beans.size())
      {
         // This case only occurs when some internal RI code wants the
         // injection point but did not push an injection point that
         // this metadata goes into.
         result = injectionPoints.peek();
      }
      else
      {
         AnnotatedMember<?, ? extends Member> currentMember = injectionPoints.pop();
         result = injectionPoints.peek();
         injectionPoints.push(currentMember);
      }
      return result;
   }
}
