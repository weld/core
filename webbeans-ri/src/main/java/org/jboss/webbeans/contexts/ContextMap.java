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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.webbeans.manager.Context;

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
      delegate = new HashMap<Class<? extends Annotation>, List<Context>>();
   }

   /**
    * Gets a list of contexts for a give scope type
    * 
    * @param key The scope type class
    * @return The list of contexts (null if none found)
    */
   public List<Context> get(Class<? extends Annotation> key)
   {
      return (List<Context>) super.get(key);
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
      StringBuffer buffer = new StringBuffer();
      buffer.append("Known scope types: " + delegate.size());
      for (Entry<Class<? extends Annotation>, List<Context>> entry : delegate.entrySet())
      {
         for (Context context : entry.getValue())
         {
            buffer.append(context.toString());
         }
      }
      return buffer.toString();
   }
}