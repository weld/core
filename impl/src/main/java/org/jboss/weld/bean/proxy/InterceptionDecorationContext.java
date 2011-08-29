/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.bean.proxy;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * A class that holds the interception (and decoration) contexts which are currently in progress.
 *
 * An interception context is a set of {@link CombinedInterceptorAndDecoratorStackMethodHandler} references for which interception is currently
 * suppressed (so that self-invocation is not possible).
 * Such references are added as soon as a CombinedMethodHandler is executed in an interception context that
 * does not hold it.
 *
 * Classes may create new interception contexts as necessary (e.g. allowing client proxies to create new interception
 * contexts in order to make circular references interceptable multiple times).
 *
 * @author Marius Bogoevici
 */
public class InterceptionDecorationContext
{
   private static ThreadLocal<List<Set<CombinedInterceptorAndDecoratorStackMethodHandler>>> interceptionContexts = new ThreadLocal<List<Set<CombinedInterceptorAndDecoratorStackMethodHandler>>>();
   
   private static Set<CombinedInterceptorAndDecoratorStackMethodHandler> pop()
   {
      List<Set<CombinedInterceptorAndDecoratorStackMethodHandler>> stack = interceptionContexts.get();
      if (stack == null)
      {
         throw new EmptyStackException();
      }
      else
      {
         try
         {
            return stack.remove(stack.size() - 1);
         }
         finally
         {
            if (stack.isEmpty())
            {
               interceptionContexts.remove();
            }
         }
      }
   }
   
   private static Set<CombinedInterceptorAndDecoratorStackMethodHandler> push(Set<CombinedInterceptorAndDecoratorStackMethodHandler> item)
   {
      List<Set<CombinedInterceptorAndDecoratorStackMethodHandler>> stack = interceptionContexts.get();
      if (stack == null)
      {
         stack = new ArrayList<Set<CombinedInterceptorAndDecoratorStackMethodHandler>>();
         interceptionContexts.set(stack);
      }
      stack.add(item);
      return item;
   }
   
   public static Set<CombinedInterceptorAndDecoratorStackMethodHandler> peek()
   {
      List<Set<CombinedInterceptorAndDecoratorStackMethodHandler>> stack = interceptionContexts.get();
      if (stack == null)
      {
         throw new EmptyStackException();
      }
      else
      {
         int last = stack.size() - 1;
         Set<CombinedInterceptorAndDecoratorStackMethodHandler> result = stack.get(last);
         if (result == null)
         {
            result = new HashSet<CombinedInterceptorAndDecoratorStackMethodHandler>();
            stack.set(last, result);
         }
         return result;
      }
   }
   
   public static boolean empty()
   {
      List<Set<CombinedInterceptorAndDecoratorStackMethodHandler>> stack = interceptionContexts.get();
      if (stack == null)
      {
         return true;
      }
      else
      {
         return stack.isEmpty();
      }
   }
   

   public static void endInterceptorContext()
   {
      pop();
   }

   public static void startInterceptorContext()
   {
      push(null);
   }
}
