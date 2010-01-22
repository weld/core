/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.el;

import java.util.Stack;

import javax.el.ELContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.Container;

class ELCreationalContextStack extends Stack<ELCreationalContext<?>>
{
   
   
   private static final Contextual<?> CONTEXTUAL = new Contextual<Object>()
   {

      public Object create(CreationalContext<Object> creationalContext)
      {
         return null;
      }

      public void destroy(Object instance, CreationalContext<Object> creationalContext) {}
      
   };
   
   private static final long serialVersionUID = -57142365866995726L;
   
   public static ELCreationalContextStack addToContext(ELContext context)
   {
      ELCreationalContextStack store = new ELCreationalContextStack();
      context.putContext(ELCreationalContextStack.class, store);
      return store;
   }
   
   
   public static ELCreationalContextStack getCreationalContextStore(ELContext context)
   {
      Object o = context.getContext(ELCreationalContextStack.class);
      
      if (!(o instanceof ELCreationalContextStack))
      {
         ELCreationalContextStack store = ELCreationalContextStack.addToContext(context);
         o = store;
      }
      ELCreationalContextStack store = (ELCreationalContextStack) o;
      if (store.isEmpty()) 
      {
         // TODO need to use correct manager for module
         ELCreationalContext<?> creationalContext = ELCreationalContext.of(Container.instance().deploymentManager().createCreationalContext(CONTEXTUAL));
         store.push(creationalContext);
      }
      return (ELCreationalContextStack) o;
   }
   
}