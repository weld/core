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
package org.jboss.webbeans.mock.el;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;

import org.jboss.webbeans.el.WebBeansELResolver;

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
   public static final ELResolver EL_RESOLVER = createELResolver();
   
   public static final ExpressionFactory EXPRESSION_FACTORY = new ExpressionFactoryImpl();
   
   private static ELResolver createELResolver()
   {
      CompositeELResolver resolver = new CompositeELResolver();
      resolver.add( new WebBeansELResolver() );
      resolver.add( new MapELResolver() );
      resolver.add( new ListELResolver() );
      resolver.add( new ArrayELResolver() );
      resolver.add( new ResourceBundleELResolver() );
      resolver.add( new BeanELResolver() );
      return resolver;
   }

   public static ELContext createELContext() {
       return createELContext( EL_RESOLVER, new FunctionMapperImpl() );
   }
   
   public static ELContext createELContext(final ELResolver resolver, final FunctionMapper functionMapper)
   {
      return new ELContext()
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
   }
   
}
