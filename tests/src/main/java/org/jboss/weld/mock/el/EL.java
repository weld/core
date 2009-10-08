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
package org.jboss.weld.mock.el;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.el.WebBeansELContextListener;
import org.jboss.weld.el.WebBeansExpressionFactory;

import com.sun.el.ExpressionFactoryImpl;
import com.sun.el.lang.FunctionMapperImpl;
import com.sun.el.lang.VariableMapperImpl;

/**
 * An instance of JBoss EL.
 * 
 * @author Gavin King
 *
 */
public class EL
{
   
   public static final ExpressionFactory EXPRESSION_FACTORY = new WebBeansExpressionFactory(new ExpressionFactoryImpl());
   
   public static final ELContextListener[] EL_CONTEXT_LISTENERS = { new WebBeansELContextListener() };
   
   private static ELResolver createELResolver(BeanManagerImpl beanManagerImpl)
   {
      CompositeELResolver resolver = new CompositeELResolver();
      resolver.add( beanManagerImpl.getELResolver() );
      resolver.add( new MapELResolver() );
      resolver.add( new ListELResolver() );
      resolver.add( new ArrayELResolver() );
      resolver.add( new ResourceBundleELResolver() );
      resolver.add( new BeanELResolver() );
      return resolver;
   }

   public static ELContext createELContext(BeanManagerImpl beanManagerImpl) {
       return createELContext(createELResolver(beanManagerImpl), new FunctionMapperImpl() );
   }
   
   public static ELContext createELContext(final ELResolver resolver, final FunctionMapper functionMapper)
   {
      ELContext context = new ELContext()
      {
         final VariableMapperImpl variableMapper = new VariableMapperImpl();

         @Override
         public ELResolver getELResolver()
         {
            return resolver;
         }

         @Override
         public FunctionMapper getFunctionMapper()
         {
            return functionMapper;
         }

         @Override
         public VariableMapper getVariableMapper()
         {
            return variableMapper;
         }
         
      };
      callELContextListeners(context);
      return context;
   }
   
   public static void callELContextListeners(ELContext context)
   {
      ELContextEvent event = new ELContextEvent(context);
      for (ELContextListener listener : EL_CONTEXT_LISTENERS)
      {
         listener.contextCreated(event);
      }
   }
   
}
