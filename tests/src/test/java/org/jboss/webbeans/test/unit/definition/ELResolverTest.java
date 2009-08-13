package org.jboss.webbeans.test.unit.definition;

import static org.testng.Assert.assertEquals;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.mock.el.EL;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * Test the WebBeansELResolver and that it collaborates with the standard EL resolver chain.
 * 
 * @author Pete Muir
 * @author Dan Allen
 */
@Artifact
public class ELResolverTest extends AbstractWebBeansTest
{
   
   /**
    * Test that the WebBeansELResolver only works to resolve the base of an EL
    * expression, in this case a named bean. Once the base is resolved, the
    * remainder of the expression should be delegated to the standard chain of
    * property resolvers. If the WebBeansELResolver oversteps its bounds by
    * trying to resolve the property against the Web Beans namespace, the test
    * will fail.
    */
   @Test
   public void testResolveBeanPropertyOfNamedBean()
   {
      ELContext elContext = EL.createELContext();
      ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;
      
      assertEquals(exprFactory.createValueExpression(elContext, "#{beer.style}", String.class).getValue(elContext), "Belgium Strong Dark Ale");
   }

   /**
    * Test that the WebBeansELResolver only works to resolve the base of an EL
    * expression, in this case from a producer method. Once the base is
    * resolved, the remainder of the expression should be delegated to the
    * standard chain of property resolvers. If the WebBeansELResolver oversteps
    * its bounds by trying to resolve the property against the Web Beans
    * namespace, the test will fail.
    */
   @Test
   public void testResolveBeanPropertyOfProducerBean()
   {
      ELContext elContext = EL.createELContext();
      ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;
      
      assertEquals(exprFactory.createValueExpression(elContext, "#{beerOnTap.style}", String.class).getValue(elContext), "IPA");
   }
   
}
