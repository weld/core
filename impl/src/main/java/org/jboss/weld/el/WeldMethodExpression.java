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
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.Container;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.el.ForwardingMethodExpression;

/**
 * @author pmuir
 *
 */
public class WeldMethodExpression extends ForwardingMethodExpression
{
   
   private static final long serialVersionUID = 7070020110515571744L;

   private static final Contextual<?> CONTEXTUAL = new Contextual<Object>()
   {

      public Object create(CreationalContext<Object> creationalContext)
      {
         return null;
      }

      public void destroy(Object instance, CreationalContext<Object> creationalContext) {}
      
   };
   
   private final MethodExpression delegate;
   
   public WeldMethodExpression(MethodExpression delegate)
   {
      this.delegate = delegate;
   }

   @Override
   protected MethodExpression delegate()
   {
      return delegate;
   }
   
   private final static BeanManagerImpl CACHED_BEANMANAGER = Container.instance().deploymentManager(); 
   
   @Override
   public Object invoke(ELContext context, Object[] params)
   {
      // TODO need to use correct manager for module
      ELCreationalContext<?> creationalContext = ELCreationalContext.of(CACHED_BEANMANAGER.createCreationalContext(CONTEXTUAL));
      try
      {
         getCreationalContextStore(context).push(creationalContext);
         return super.invoke(context, params);
      }
      finally
      {
         getCreationalContextStore(context).pop();
         creationalContext.release();
      }
   }
   
   @Override
   public MethodInfo getMethodInfo(ELContext context)
   {
      // TODO need to use correct manager for module
      ELCreationalContext<?> creationalContext = ELCreationalContext.of(CACHED_BEANMANAGER.createCreationalContext(CONTEXTUAL));
      try
      {
         getCreationalContextStore(context).push(creationalContext);
         return super.getMethodInfo(context);
      }
      finally
      {
         getCreationalContextStore(context).pop();
         creationalContext.release();
      }
   }

}
