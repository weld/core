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

import static org.jboss.weld.el.ELCreationalContextStack.getCreationalContextStore;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.Container;
import org.jboss.weld.util.el.ForwardingValueExpression;

/**
 * @author pmuir
 *
 */
public class WeldValueExpression extends ForwardingValueExpression
{
   
   private static final long serialVersionUID = 1122137212009930853L;

   private static final Contextual<?> CONTEXTUAL = new Contextual<Object>()
   {

      public Object create(CreationalContext<Object> creationalContext)
      {
         return null;
      }

      public void destroy(Object instance, CreationalContext<Object> creationalContext) {}
      
   };
   
   private final ValueExpression delegate;
   
   public WeldValueExpression(ValueExpression delegate)
   {
      this.delegate = delegate;
   }

   @Override
   protected ValueExpression delegate()
   {
      return delegate;
   }
   
   @Override
   public Object getValue(final ELContext context)
   {
      // TODO need to use correct manager for module
      ELCreationalContext<?> creationalContext = ELCreationalContext.of(Container.instance().deploymentManager().createCreationalContext(CONTEXTUAL));
      try
      {
         getCreationalContextStore(context).push(creationalContext);
         return delegate().getValue(context);
      }
      finally
      {
         getCreationalContextStore(context).pop();
         creationalContext.release();
      }
   }
   
   @Override
   public void setValue(ELContext context, Object value)
   {
      // TODO need to use correct manager for module
      ELCreationalContext<?> creationalContext = ELCreationalContext.of(Container.instance().deploymentManager().createCreationalContext(CONTEXTUAL));
      try
      {
         getCreationalContextStore(context).push(creationalContext);
         delegate().setValue(context, value);
      }
      finally
      {
         getCreationalContextStore(context).pop();
         creationalContext.release();
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   public Class getType(ELContext context)
   {
   // TODO need to use correct manager for module
      ELCreationalContext<?> creationalContext = ELCreationalContext.of(Container.instance().deploymentManager().createCreationalContext(CONTEXTUAL));
      try
      {
         getCreationalContextStore(context).push(creationalContext);
         return delegate().getType(context);
      }
      finally
      {
         getCreationalContextStore(context).pop();
         creationalContext.release();
      }
   }

}
