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
