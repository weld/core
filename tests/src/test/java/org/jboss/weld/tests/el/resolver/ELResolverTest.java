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
package org.jboss.weld.tests.el.resolver;

import static org.testng.Assert.assertEquals;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.mock.el.EL;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * Test the WeldELResolver and that it collaborates with the standard EL resolver chain.
 * 
 * @author Pete Muir
 * @author Dan Allen
 */
@Artifact
public class ELResolverTest extends AbstractWeldTest
{
   
   /**
    * Test that the WeldELResolver only works to resolve the base of an EL
    * expression, in this case a named bean. Once the base is resolved, the
    * remainder of the expression should be delegated to the standard chain of
    * property resolvers. If the WeldELResolver oversteps its bounds by
    * trying to resolve the property against the Weld namespace, the test
    * will fail.
    */
   @Test
   public void testResolveBeanPropertyOfNamedBean()
   {
      ELContext elContext = EL.createELContext(getCurrentManager());
      ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;
      
      assertEquals(exprFactory.createValueExpression(elContext, "#{beer.style}", String.class).getValue(elContext), "Belgium Strong Dark Ale");
   }

   /**
    * Test that the WeldELResolver only works to resolve the base of an EL
    * expression, in this case from a producer method. Once the base is
    * resolved, the remainder of the expression should be delegated to the
    * standard chain of property resolvers. If the WeldELResolver oversteps
    * its bounds by trying to resolve the property against the Weld
    * namespace, the test will fail.
    */
   @Test
   public void testResolveBeanPropertyOfProducerBean()
   {
      ELContext elContext = EL.createELContext(getCurrentManager());
      ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;
      
      assertEquals(exprFactory.createValueExpression(elContext, "#{beerOnTap.style}", String.class).getValue(elContext), "IPA");
   }
   
}
