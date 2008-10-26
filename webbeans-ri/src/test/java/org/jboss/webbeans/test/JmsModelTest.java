package org.jboss.webbeans.test;

import javax.webbeans.DefinitionException;

import org.testng.annotations.Test;

public class JmsModelTest extends AbstractTest
{
   
   @Test(expectedExceptions=DefinitionException.class, groups="jms") @SpecAssertion(section="2.6")
   public void testJmsEndpointHasName()
   {
      assert false;
   }
   
}
