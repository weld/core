/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.international;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.el.EL;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class InternationalTest 
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
                  .addPackage(InternationalTest.class.getPackage());
   }
   
   @Test
   // WELD-642
   public void testNoInterface(BeanManagerImpl beanManager)
   {
      assertEquals(1, beanManager.getBeans("käyttäjä").size());
      ELContext elContext = EL.createELContext(beanManager);
      ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;
      
      Object value = exprFactory.createValueExpression(elContext, "#{käyttäjä}", Object.class).getValue(elContext);
      
      assertNotNull(value);
      assertTrue(Käyttäjä.class.isAssignableFrom(value.getClass()));
   }
}
