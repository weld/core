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

package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.webbeans.manager.Context;

import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

/**
 * A map from a scope to a list of contexts
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
public class ContextMap extends ForwardingMap<Class<? extends Annotation>, List<Context>>
{
   private Map<Class<? extends Annotation>, List<Context>> delegate;

   public ContextMap()
   {
      delegate = new ConcurrentHashMap<Class<? extends Annotation>, List<Context>>();
   }

   /**
    * Gets the dependent context
    * 
    * @param scopeType The scope type to get
    * @return The dependent context
    */
   public DependentContext getBuiltInContext(Class<? extends Annotation> scopeType)
   {
      // TODO Why can we request any scopetype and the cast it to dependent?
      return (DependentContext) get(scopeType).iterator().next();
   }

   /**
    * Returns the delegate of the forwarding map
    * 
    * @return the delegate
    */
   @Override
   protected Map<Class<? extends Annotation>, List<Context>> delegate()
   {
      return delegate;
   }

   @Override
   public String toString()
   {
      return Strings.mapToString("ContextMap (scope type -> context list): ", delegate);
   }

   /**
    * Adds a context under a scope type
    * 
    * Creates the list of contexts if it doesn't exist
    * 
    * @param context The new context
    */
   public void add(Context context)
   {
      List<Context> contexts = super.get(context.getScopeType());
      if (contexts == null)
      {
         synchronized (delegate)
         {
            contexts = new CopyOnWriteArrayList<Context>();
            put(context.getScopeType(), contexts);
         }
         contexts = super.get(context.getScopeType());
      }
      contexts.add(context);
   }

   /**
    * Gets a context list for a scope type
    * 
    * @param scopeType The scope type
    * @return A list of contexts. An empty list is returned if there are no registered scopes of this type
    */
   public List<Context> get(Class<? extends Annotation> scopeType)
   {
      List<Context> contexts = super.get(scopeType);
      return contexts != null ? contexts : new ArrayList<Context>();
   }
   
   

}